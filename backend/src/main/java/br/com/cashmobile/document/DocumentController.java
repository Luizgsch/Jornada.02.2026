package br.com.cashmobile.document;

import br.com.cashmobile.auth.DemoSessions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Documentos", description = "Um anexo PDF, PNG ou JPEG por movimentação ativa; até 5 MiB por arquivo, em memória. Lixeira oculta o anexo e restauração recupera sua visibilidade.")
@ApiResponse(responseCode = "400", description = "Arquivo, filtros ou identificador inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "401", description = "Sessão inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "404", description = "Movimentação ativa ou anexo não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
public class DocumentController {
    private final DocumentService service;
    public DocumentController(DocumentService service) { this.service = service; }

    @PutMapping(value = "/movements/{id}/attachment", consumes = "multipart/form-data", produces = "application/json")
    @Operation(summary = "Adicionar ou substituir anexo", description = "Campo multipart file. Substituição é atômica: erro preserva o arquivo anterior. Nome é sanitizado, tipo e assinatura básica conferidos. Não aceita envio à lixeira.")
    @ApiResponse(responseCode = "200", description = "Metadados do anexo salvo")
    @ApiResponse(responseCode = "413", description = "Arquivo acima de 5 MiB ou requisição acima de 6 MiB", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "415", description = "Tipo não permitido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "503", description = "Limite global de memória para anexos atingido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public DocumentService.DocumentInfo upload(@AuthenticationPrincipal DemoSessions.Session session,
            @PathVariable UUID id, @RequestPart("file") MultipartFile file) { return service.upload(session.user().email(), id, file); }

    @GetMapping(value = "/movements/{id}/attachment", produces = {"application/pdf", "image/png", "image/jpeg"})
    @Operation(summary = "Baixar conteúdo do anexo", description = "Retorna bytes com Content-Type e Content-Disposition. Exige Authorization; a URL sozinha não autoriza acesso.")
    @ApiResponse(responseCode = "200", description = "Conteúdo binário", content = {
            @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary")),
            @Content(mediaType = "image/png", schema = @Schema(type = "string", format = "binary")),
            @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary"))})
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal DemoSessions.Session session, @PathVariable UUID id) {
        var file = service.get(session.user().email(), id).attachment();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.contentType())).contentLength(file.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.name(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store").body(file.content());
    }

    @DeleteMapping("/movements/{id}/attachment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover anexo da movimentação ativa", description = "Remove somente o arquivo e libera sua memória. Esta remoção não tem lixeira própria e não apaga a movimentação. Repetir retorna 204.")
    @ApiResponse(responseCode = "204", description = "Anexo removido")
    public void remove(@AuthenticationPrincipal DemoSessions.Session session, @PathVariable UUID id) { service.remove(session.user().email(), id); }

    @GetMapping(value = "/documents", produces = "application/json")
    @Operation(summary = "Pesquisar documentos da conta", description = "Metadados de anexos de registros ativos. Mês filtra a data da movimentação, não a do upload. Não inclui bytes na listagem.")
    @ApiResponse(responseCode = "200", description = "Página de metadados, sem conteúdo binário")
    public DocumentService.DocumentPage list(@AuthenticationPrincipal DemoSessions.Session session,
            @RequestParam(required = false) @Size(max = 160) String q,
            @RequestParam(defaultValue = "all") DocumentService.Kind kind,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(session.user().email(), q, kind, month, page, size);
    }
}
