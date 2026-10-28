package br.com.cashmobile.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DemoSessions {
    private final AuthenticationManager authenticationManager;
    private final Clock clock;
    private final Duration ttl;
    private final int maxSessions;
    private final SecureRandom random = new SecureRandom();
    // Somente o hash do token é retido no servidor.
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public DemoSessions(AuthenticationManager authenticationManager, Clock clock,
            @Value("${cash.auth.session-ttl:PT30M}") Duration ttl,
            @Value("${cash.auth.max-sessions:1000}") int maxSessions) {
        if (ttl.isNegative() || ttl.isZero() || maxSessions < 1) {
            throw new IllegalArgumentException("Configuração de sessões inválida");
        }
        this.authenticationManager = authenticationManager;
        this.clock = clock;
        this.ttl = ttl;
        this.maxSessions = maxSessions;
    }

    public Grant login(String email, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("Credenciais inválidas");
        }
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        return issue();
    }

    private synchronized Grant issue() {
        Instant now = clock.instant();
        sessions.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        if (sessions.size() >= maxSessions) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Limite de sessões atingido. Tente mais tarde.");
        }
        byte[] bytes = new byte[32];
        String token;
        String key;
        do {
            random.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            key = hash(token);
        } while (sessions.containsKey(key));
        var session = new Session(new DemoUser(DemoAccount.EMAIL, "Usuário de demonstração", "DEMO"), now.plus(ttl));
        sessions.put(key, session);
        return new Grant(token, "Bearer", session.expiresAt(), session.user());
    }

    public Session require(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw invalidSession();
        String key = hash(token);
        var session = sessions.get(key);
        if (session == null) throw invalidSession();
        if (!session.expiresAt().isAfter(clock.instant())) {
            sessions.remove(key, session);
            throw invalidSession();
        }
        return session;
    }

    public void logout(String token) {
        sessions.remove(hash(token));
    }

    private BadCredentialsException invalidSession() {
        return new BadCredentialsException("Sessão inválida ou expirada");
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }

    public record DemoUser(String email, String name, String role) {}
    public record Session(DemoUser user, Instant expiresAt) {}
    public record Grant(String accessToken, String tokenType, Instant expiresAt, DemoUser user) {}
}
