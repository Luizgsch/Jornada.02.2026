package br.com.cashmobile.movement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

// Contrato de armazenamento separado do controller e das regras de negócio.
public interface MovementRepository {
    Movement create(String owner, Movement movement);
    Optional<Movement> find(String owner, UUID id);
    List<Movement> findAll(String owner);
    Movement change(String owner, UUID id, UnaryOperator<Movement> update);
    AttachedMovement putAttachment(String owner, UUID id, MovementAttachment attachment);
    Optional<AttachedMovement> attachment(String owner, UUID id);
    void removeAttachment(String owner, UUID id);
    List<AttachedMovement> attachments(String owner);
    record AttachedMovement(Movement movement, MovementAttachment attachment) {}
}
