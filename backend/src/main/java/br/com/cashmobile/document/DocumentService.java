package br.com.cashmobile.document;

import br.com.cashmobile.movement.MovementAttachment;
import br.com.cashmobile.movement.MovementRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentService {
    private static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
    public enum Kind { all, pdf, image }
    private final MovementRepository repository;
    private final Clock clock;
    public DocumentService(MovementRepository repository, Clock clock) { this.repository = repository; this.clock = clock; }

    public DocumentInfo upload(String owner, UUID id, MultipartFile file) {
        if (file.isEmpty()) throw failure(HttpStatus.BAD_REQUEST, "Anexo vazio.");
        if (file.getSize() > MAX_FILE_BYTES) throw failure(HttpStatus.PAYLOAD_TOO_LARGE, "Anexo deve ter no máximo 5 MiB.");
        String mime;
        try {
            var type = MediaType.parseMediaType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            mime = (type.getType() + "/" + type.getSubtype()).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException error) {
            throw failure(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de arquivo inválido.");
        }
        if (!List.of("application/pdf", "image/png", "image/jpeg").contains(mime)) {
            throw failure(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Use somente PDF, PNG ou JPEG.");
        }
        byte[] bytes;
        try { bytes = file.getBytes(); }
        catch (IOException error) { throw failure(HttpStatus.BAD_REQUEST, "Não foi possível ler o anexo."); }
        validateContent(bytes, mime);
        var attachment = new MovementAttachment(filename(file.getOriginalFilename(), mime), mime, bytes, clock.instant());
        return info(repository.putAttachment(owner, id, attachment));
    }

    private void validateContent(byte[] bytes, String mime) {
        boolean signature = switch (mime) {
            case "application/pdf" -> bytes.length >= 8 && new String(bytes, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-")
                    && new String(bytes, Math.max(0, bytes.length - 1024), Math.min(bytes.length, 1024), StandardCharsets.ISO_8859_1).contains("%%EOF");
            case "image/png" -> bytes.length >= 8 && bytes[0] == (byte) 137 && bytes[1] == 80 && bytes[2] == 78
                    && bytes[3] == 71 && bytes[4] == 13 && bytes[5] == 10 && bytes[6] == 26 && bytes[7] == 10;
            case "image/jpeg" -> bytes.length >= 3 && bytes[0] == (byte) 255 && bytes[1] == (byte) 216 && bytes[2] == (byte) 255;
            default -> false;
        };
        if (!signature) throw failure(HttpStatus.BAD_REQUEST, "Conteúdo não corresponde ao tipo de arquivo informado.");
        if (!mime.equals("application/pdf")) {
            try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw failure(HttpStatus.BAD_REQUEST, "Cabeçalho de imagem inválido.");
                var reader = readers.next();
                try {
                    reader.setInput(input, true, true);
                    int width = reader.getWidth(0), height = reader.getHeight(0);
                    if (width < 1 || height < 1 || (long) width * height > 20_000_000) {
                        throw failure(HttpStatus.BAD_REQUEST, "Imagem deve ter no máximo 20 milhões de pixels.");
                    }
                } finally { reader.dispose(); }
            } catch (IOException | IllegalArgumentException error) {
                throw failure(HttpStatus.BAD_REQUEST, "Cabeçalho de imagem inválido.");
            }
        }
    }

    private String filename(String original, String mime) {
        String name = original == null ? "documento" : original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[^\\p{L}\\p{N} ._()-]", "_").strip().replaceFirst("^\\.+", "");
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);
        if (name.isBlank()) name = "documento";
        name = name.substring(0, Math.min(100, name.length()));
        return name + (mime.equals("application/pdf") ? ".pdf" : mime.equals("image/png") ? ".png" : ".jpg");
    }

    public MovementRepository.AttachedMovement get(String owner, UUID id) {
        return repository.attachment(owner, id).orElseThrow(() -> failure(HttpStatus.NOT_FOUND, "Anexo não encontrado."));
    }

    public void remove(String owner, UUID id) { repository.removeAttachment(owner, id); }

    public DocumentPage list(String owner, String q, Kind kind, String month, int page, int size) {
        YearMonth period = null;
        if (month != null) {
            try {
                period = YearMonth.parse(month);
                if (period.getYear() < 1900 || period.getYear() > 9999) throw new DateTimeParseException("Ano inválido", month, 0);
            } catch (DateTimeParseException error) { throw failure(HttpStatus.BAD_REQUEST, "Mês inválido. Use YYYY-MM entre 1900-01 e 9999-12."); }
        }
        YearMonth filterMonth = period;
        String search = q == null ? "" : q.strip().toLowerCase(Locale.ROOT);
        var items = repository.attachments(owner).stream()
                .filter(item -> filterMonth == null || YearMonth.from(item.movement().date()).equals(filterMonth))
                .filter(item -> kind == Kind.all || (kind == Kind.pdf) == item.attachment().contentType().equals("application/pdf"))
                .filter(item -> (item.attachment().name() + " " + item.movement().description() + " " + item.movement().category())
                        .toLowerCase(Locale.ROOT).contains(search))
                .map(this::info).sorted(Comparator.comparing(DocumentInfo::movementDate).reversed()
                        .thenComparing(DocumentInfo::uploadedAt, Comparator.reverseOrder()).thenComparing(DocumentInfo::movementId)).toList();
        int start = (int) Math.min((long) page * size, items.size());
        return new DocumentPage(items.subList(start, Math.min(start + size, items.size())), items.size(), page, size,
                (items.size() + size - 1) / size);
    }

    private DocumentInfo info(MovementRepository.AttachedMovement item) {
        return new DocumentInfo(item.movement().id(), item.movement().description(), item.movement().date(),
                item.attachment().name(), item.attachment().contentType(), item.attachment().size(), item.attachment().uploadedAt(),
                "/api/v1/movements/" + item.movement().id() + "/attachment");
    }

    private ResponseStatusException failure(HttpStatus status, String detail) { return new ResponseStatusException(status, detail); }
    public record DocumentInfo(UUID movementId, String movementDescription, LocalDate movementDate, String name,
            String contentType, long size, Instant uploadedAt, String downloadPath) {}
    public record DocumentPage(List<DocumentInfo> items, long total, int page, int size, int totalPages) {}
}
