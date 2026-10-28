package br.com.cashmobile.movement;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MovementService {
    private final MovementRepository repository;
    private final Clock clock;
    public MovementService(MovementRepository repository, Clock clock) { this.repository = repository; this.clock = clock; }

    public Movement create(String owner, MovementInput input) {
        checkDate(input);
        var now = clock.instant();
        return repository.create(owner, new Movement(UUID.randomUUID(), input.type(), input.description().strip(),
                input.valueInCents().longValueExact(), input.date(), input.category(), input.account(),
                input.notes() == null ? "" : input.notes().strip(), now, now, null));
    }

    public Movement get(String owner, UUID id) {
        return repository.find(owner, id).filter(item -> item.deletedAt() == null)
                .orElseThrow(() -> new MovementFailure(MovementFailure.Reason.NOT_FOUND, "Movimentação não encontrada."));
    }

    public Movement update(String owner, UUID id, MovementInput input) {
        checkDate(input);
        return repository.change(owner, id, current -> {
            if (current.deletedAt() != null) throw new MovementFailure(MovementFailure.Reason.CONFLICT,
                    "Restaure a movimentação antes de editar.");
            return new Movement(current.id(), input.type(), input.description().strip(), input.valueInCents().longValueExact(),
                    input.date(), input.category(), input.account(), input.notes() == null ? "" : input.notes().strip(),
                    current.createdAt(), clock.instant(), null);
        });
    }

    public void trash(String owner, UUID id) {
        repository.change(owner, id, current -> {
            if (current.deletedAt() != null) return current;
            var now = clock.instant();
            return new Movement(current.id(), current.type(), current.description(), current.valueInCents(), current.date(),
                    current.category(), current.account(), current.notes(), current.createdAt(), now, now);
        });
    }

    public Movement restore(String owner, UUID id) {
        return repository.change(owner, id, current -> {
            if (current.deletedAt() == null) throw new MovementFailure(MovementFailure.Reason.CONFLICT,
                    "Movimentação já está ativa.");
            return new Movement(current.id(), current.type(), current.description(), current.valueInCents(), current.date(),
                    current.category(), current.account(), current.notes(), current.createdAt(), clock.instant(), null);
        });
    }

    public Page list(String owner, boolean deleted, Movement.MovementType type, String q,
            LocalDate from, LocalDate to, int page, int size) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new MovementFailure(MovementFailure.Reason.INVALID, "A data inicial deve ser anterior ou igual à final.");
        }
        String search = q == null ? "" : q.strip().toLowerCase(Locale.ROOT);
        var items = repository.findAll(owner).stream()
                .filter(item -> (item.deletedAt() != null) == deleted)
                .filter(item -> type == null || item.type() == type)
                .filter(item -> from == null || !item.date().isBefore(from))
                .filter(item -> to == null || !item.date().isAfter(to))
                .filter(item -> (item.description() + " " + item.category()).toLowerCase(Locale.ROOT).contains(search))
                .sorted(Comparator.comparing(Movement::date).reversed()
                        .thenComparing(Movement::createdAt, Comparator.reverseOrder()).thenComparing(Movement::id))
                .toList();
        int start = (int) Math.min((long) page * size, items.size());
        int end = Math.min(start + size, items.size());
        return new Page(items.subList(start, end), items.size(), page, size, (items.size() + size - 1) / size);
    }

    private void checkDate(MovementInput input) {
        if (input.date().getYear() < 1900 || input.date().getYear() > 9999) {
            throw new MovementFailure(MovementFailure.Reason.INVALID, "Data deve estar entre 1900-01-01 e 9999-12-31.");
        }
    }

    public record Page(List<Movement> items, long total, int page, int size, int totalPages) {}
}
