package br.com.cashmobile;

import br.com.cashmobile.finance.FinanceService;
import br.com.cashmobile.movement.*;
import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AttachmentStorageTests {
    @Test
    void quotaOwnershipAndDefensiveCopiesSurviveChangesAndTrash() {
        var repository = new InMemoryMovementRepository(10, 10);
        var service = new MovementService(repository, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        var input = new MovementInput(Movement.MovementType.entrada, "Fictício", new BigDecimal("12345"),
                LocalDate.of(2026,10,28), "Outros", "Conta principal", "");
        var first = service.create("a", input);
        var second = service.create("a", input);
        byte[] bytes = {1,2,3,4,5,6};
        var file = new MovementAttachment("f.pdf", "application/pdf", bytes, Instant.EPOCH);
        bytes[0] = 99;
        repository.putAttachment("a", first.id(), file);
        byte[] returned = repository.attachment("a", first.id()).orElseThrow().attachment().content();
        assertThat(returned[0]).isEqualTo((byte) 1);
        returned[0] = 88;
        assertThat(file.content()[0]).isEqualTo((byte) 1);
        assertThatThrownBy(() -> repository.attachment("b", first.id())).isInstanceOf(MovementFailure.class);
        assertThatThrownBy(() -> repository.putAttachment("b", first.id(), file)).isInstanceOf(MovementFailure.class);
        assertThatThrownBy(() -> repository.removeAttachment("b", first.id())).isInstanceOf(MovementFailure.class);
        assertThat(repository.attachments("b")).isEmpty();
        assertThatThrownBy(() -> repository.putAttachment("a", second.id(), file)).isInstanceOfSatisfying(MovementFailure.class,
                error -> assertThat(error.reason()).isEqualTo(MovementFailure.Reason.CAPACITY));
        repository.putAttachment("a", first.id(), file); // substituir não soma o mesmo arquivo duas vezes
        service.update("a", first.id(), input);
        assertThat(repository.attachment("a", first.id())).isPresent();
        service.trash("a", first.id());
        assertThat(repository.attachments("a")).isEmpty();
        assertThatThrownBy(() -> repository.putAttachment("a", second.id(), file)).isInstanceOf(MovementFailure.class);
        service.restore("a", first.id());
        repository.removeAttachment("a", first.id());
        repository.putAttachment("a", second.id(), file);
        assertThat(repository.attachments("a")).hasSize(1);
        assertThat(new FinanceService(repository).summary("b", "2026-10").balanceCents()).isZero();
        assertThat(new InMemoryMovementRepository(10, 10).attachments("a")).isEmpty();
    }
}
