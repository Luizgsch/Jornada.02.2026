package br.com.cashmobile;

import br.com.cashmobile.profile.*;
import br.com.cashmobile.notification.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UserStateStorageTests {
    @Test
    void profilesPreferencesAndReadStateAreIsolatedAndResetOnRestart() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), ZoneOffset.UTC);
        var profiles = new ProfileService(new InMemoryProfileRepository(clock), clock);
        profiles.preferences("a", false, true);
        profiles.update("a", new ProfileInput("Teste", "teste@example.com", null, "Exemplo"));
        assertThat(profiles.preferences("a").reduceMotion()).isTrue();
        assertThat(profiles.get("a").phone()).isEmpty();
        assertThat(profiles.get("b").name()).isEqualTo("Fulano de Lima");
        assertThat(profiles.preferences("b").hideBalance()).isTrue();
        assertThat(profiles.preferences("b").reduceMotion()).isFalse();
        var alerts = new NotificationService(new InMemoryNotificationRepository(), clock);
        alerts.readAll("a");
        assertThat(alerts.count("a").unreadCount()).isZero();
        assertThat(alerts.count("b").unreadCount()).isEqualTo(2);
        alerts.read("a", "documento", false);
        assertThat(alerts.get("a", "documento").readAt()).isNull();
        assertThat(alerts.get("b", "documento").read()).isTrue();
        assertThat(new ProfileService(new InMemoryProfileRepository(clock), clock).get("a").name()).isEqualTo("Fulano de Lima");
        assertThat(new NotificationService(new InMemoryNotificationRepository(), clock).count("a").unreadCount()).isEqualTo(2);
    }
}
