package br.com.cashmobile;

import br.com.cashmobile.movement.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class MovementStorageTests {
    @Test
    void ownershipCapacityTrashAndRestartStayIndependentOfHttp() {
        var repository = new InMemoryMovementRepository(1);
        var service = new MovementService(repository, Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), ZoneOffset.UTC));
        var input = new MovementInput(Movement.MovementType.entrada, "Demonstração", new BigDecimal("12345"),
                LocalDate.of(2026, 10, 28), "Outros", "Conta principal", null);
        var created = service.create("owner-a", input);
        assertThat(service.list("owner-b", false, null, null, null, null, 0, 20).total()).isZero();
        assertThatThrownBy(() -> service.get("owner-b", created.id())).isInstanceOf(MovementFailure.class);
        assertThatThrownBy(() -> service.update("owner-b", created.id(), input)).isInstanceOf(MovementFailure.class);
        assertThatThrownBy(() -> service.trash("owner-b", created.id())).isInstanceOf(MovementFailure.class);
        assertThatThrownBy(() -> service.restore("owner-b", created.id())).isInstanceOf(MovementFailure.class);
        service.trash("owner-a", created.id());
        assertThatThrownBy(() -> service.create("owner-a", input)).isInstanceOfSatisfying(MovementFailure.class,
                error -> assertThat(error.reason()).isEqualTo(MovementFailure.Reason.CAPACITY));
        service.restore("owner-a", created.id());
        assertThat(service.get("owner-a", created.id()).valueInCents()).isEqualTo(12345);
        var restarted = new InMemoryMovementRepository(1);
        assertThat(restarted.findAll("owner-a")).isEmpty();
    }
}
