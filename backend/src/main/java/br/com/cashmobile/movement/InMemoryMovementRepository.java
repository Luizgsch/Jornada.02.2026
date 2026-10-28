package br.com.cashmobile.movement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryMovementRepository implements MovementRepository {
    private record Entry(String owner, Movement movement, MovementAttachment attachment) {}
    private final Map<UUID, Entry> entries = new HashMap<>();
    private final int limit;
    private final long maxAttachmentBytes;
    private long attachmentBytes;

    public InMemoryMovementRepository(int limit) { this(limit, 50L * 1024 * 1024); }

    @Autowired
    public InMemoryMovementRepository(@Value("${cash.movements.max-records:10000}") int limit,
            @Value("${cash.documents.max-total-bytes:52428800}") long maxAttachmentBytes) {
        if (limit < 1 || maxAttachmentBytes < 1) throw new IllegalArgumentException("Limites de memória devem ser positivos");
        this.limit = limit;
        this.maxAttachmentBytes = maxAttachmentBytes;
    }

    @Override
    public synchronized Movement create(String owner, Movement movement) {
        if (entries.size() >= limit) throw new MovementFailure(MovementFailure.Reason.CAPACITY,
                "Limite de registros da demonstração atingido. Registros na lixeira também ocupam espaço.");
        if (entries.containsKey(movement.id())) throw new MovementFailure(MovementFailure.Reason.CONFLICT, "Registro já existe.");
        entries.put(movement.id(), new Entry(owner, movement, null));
        return movement;
    }

    @Override
    public synchronized Optional<Movement> find(String owner, UUID id) {
        var entry = entries.get(id);
        return entry != null && entry.owner().equals(owner) ? Optional.of(entry.movement()) : Optional.empty();
    }

    @Override
    public synchronized List<Movement> findAll(String owner) {
        return entries.values().stream().filter(entry -> entry.owner().equals(owner)).map(Entry::movement).toList();
    }

    @Override
    public synchronized Movement change(String owner, UUID id, UnaryOperator<Movement> update) {
        Movement current = find(owner, id).orElseThrow(() -> new MovementFailure(
                MovementFailure.Reason.NOT_FOUND, "Movimentação não encontrada."));
        Movement changed = update.apply(current);
        entries.put(id, new Entry(owner, changed, entries.get(id).attachment()));
        return changed;
    }

    private Entry active(String owner, UUID id) {
        var entry = entries.get(id);
        if (entry == null || !entry.owner().equals(owner) || entry.movement().deletedAt() != null) {
            throw new MovementFailure(MovementFailure.Reason.NOT_FOUND, "Movimentação ativa não encontrada.");
        }
        return entry;
    }

    @Override
    public synchronized AttachedMovement putAttachment(String owner, UUID id, MovementAttachment attachment) {
        var entry = active(owner, id);
        long oldSize = entry.attachment() == null ? 0 : entry.attachment().size();
        long nextSize = attachmentBytes - oldSize + attachment.size();
        if (nextSize > maxAttachmentBytes) throw new MovementFailure(MovementFailure.Reason.CAPACITY,
                "Limite de memória para anexos atingido. Remova anexos que não precisa antes de tentar novamente.");
        entries.put(id, new Entry(owner, entry.movement(), attachment));
        attachmentBytes = nextSize;
        return new AttachedMovement(entry.movement(), attachment);
    }

    @Override
    public synchronized Optional<AttachedMovement> attachment(String owner, UUID id) {
        var entry = active(owner, id);
        return entry.attachment() == null ? Optional.empty() : Optional.of(new AttachedMovement(entry.movement(), entry.attachment()));
    }

    @Override
    public synchronized void removeAttachment(String owner, UUID id) {
        var entry = active(owner, id);
        if (entry.attachment() != null) {
            attachmentBytes -= entry.attachment().size();
            entries.put(id, new Entry(owner, entry.movement(), null));
        }
    }

    @Override
    public synchronized List<AttachedMovement> attachments(String owner) {
        return entries.values().stream().filter(entry -> entry.owner().equals(owner)
                && entry.movement().deletedAt() == null && entry.attachment() != null)
                .map(entry -> new AttachedMovement(entry.movement(), entry.attachment())).toList();
    }
}
