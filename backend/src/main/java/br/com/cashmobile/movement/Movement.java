package br.com.cashmobile.movement;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Movement(UUID id, MovementType type, String description, long valueInCents,
        LocalDate date, String category, String account, String notes,
        Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public enum MovementType { entrada, saida }
}
