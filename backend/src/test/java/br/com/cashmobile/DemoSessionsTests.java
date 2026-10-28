package br.com.cashmobile;

import br.com.cashmobile.auth.DemoSessions;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemoSessionsTests {
    static class TestClock extends Clock {
        Instant now = Instant.parse("2026-10-28T12:00:00Z");
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void expirationCapacityAndRestartAreEnforcedWithoutWaiting() {
        var clock = new TestClock();
        var sessions = new DemoSessions(authentication -> authentication, clock, Duration.ofMinutes(30), 1);
        var first = sessions.login("demo@cash.local", "Cash123!");
        assertThat(sessions.require(first.accessToken()).user().email()).isEqualTo("demo@cash.local");
        assertThatThrownBy(() -> sessions.login("demo@cash.local", "Cash123!"))
                .isInstanceOf(ResponseStatusException.class);
        clock.now = clock.now.plus(Duration.ofMinutes(30));
        assertThatThrownBy(() -> sessions.require(first.accessToken())).isInstanceOf(BadCredentialsException.class);
        var next = sessions.login("demo@cash.local", "Cash123!");
        assertThat(next.accessToken()).isNotEqualTo(first.accessToken());
        var restarted = new DemoSessions(authentication -> authentication, clock, Duration.ofMinutes(30), 1);
        assertThatThrownBy(() -> restarted.require(next.accessToken())).isInstanceOf(BadCredentialsException.class);
    }
}
