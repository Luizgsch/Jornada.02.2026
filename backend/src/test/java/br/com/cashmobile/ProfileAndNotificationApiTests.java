package br.com.cashmobile;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class ProfileAndNotificationApiTests {
    @LocalServerPort private int port;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();
    private HttpResponse<String> call(String method, String path, Object body, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(20));
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (body != null) request.header("Content-Type", "application/json");
        return client.send(request.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(), HttpResponse.BodyHandlers.ofString());
    }
    private String login() throws Exception {
        var response = call("POST", "/api/v1/auth/login", Map.of("email", "demo@cash.local", "password", "Cash123!"), null);
        assertThat(response.statusCode()).isEqualTo(200);
        return json.readTree(response.body()).path("accessToken").asString();
    }
    private JsonNode get(String path, String token) throws Exception {
        var response = call("GET", path, null, token);
        assertThat(response.statusCode()).isEqualTo(200);
        return json.readTree(response.body());
    }

    @Test
    void profileUpdatesPreservePreferencesAndCannotChangeAuthentication() throws Exception {
        String token = login(), second = login();
        var original = get("/api/v1/profile", token);
        var prefs = get("/api/v1/profile/preferences", token);
        assertThat(call("PUT", "/api/v1/profile/preferences", Map.of("hideBalance", false, "reduceMotion", true), token).statusCode()).isEqualTo(200);
        var edit = new LinkedHashMap<String,Object>(Map.of("name", "  Nome fictício  ", "email", "contato@example.com",
                "phone", "", "office", "  Cartório de teste  ", "loginEmail", "changed@example.com", "role", "ADMIN", "owner", "other"));
        var response = call("PUT", "/api/v1/profile", edit, token);
        assertThat(response.statusCode()).isEqualTo(200);
        var changed = get("/api/v1/profile", second);
        assertThat(changed.path("name").asString()).isEqualTo("Nome fictício");
        assertThat(changed.path("email").asString()).isEqualTo("contato@example.com");
        assertThat(changed.path("loginEmail").asString()).isEqualTo("demo@cash.local");
        assertThat(changed.path("role").asString()).isEqualTo("DEMO");
        assertThat(get("/api/v1/profile/preferences", second).path("reduceMotion").asBoolean()).isTrue();
        assertThat(get("/api/v1/profile/preferences", token).path("hideBalance").asBoolean()).isFalse();
        assertThat(get("/api/v1/auth/me", token).path("user").path("role").asString()).isEqualTo("DEMO");
        assertThat(login()).isNotBlank();
        call("PUT", "/api/v1/profile", Map.of("name", original.path("name").asString(), "email", original.path("email").asString(),
                "phone", original.path("phone").asString(), "office", original.path("office").asString()), token);
        call("PUT", "/api/v1/profile/preferences", Map.of("hideBalance", prefs.path("hideBalance").asBoolean(), "reduceMotion", prefs.path("reduceMotion").asBoolean()), token);
    }

    @Test
    void profileValidationAndAuthenticationAreEnforced() throws Exception {
        String token = login();
        for (var patch : List.of(Map.<String,Object>of("name", " "), Map.<String,Object>of("email", "bad"),
                Map.<String,Object>of("phone", "not-a-phone"), Map.<String,Object>of("office", "a".repeat(121)))) {
            var body = new LinkedHashMap<String,Object>(Map.of("name", "Teste", "email", "teste@example.com", "office", "Exemplo"));
            body.putAll(patch);
            assertThat(call("PUT", "/api/v1/profile", body, token).statusCode()).isEqualTo(400);
        }
        assertThat(call("PUT", "/api/v1/profile", Map.of(), token).statusCode()).isEqualTo(400);
        assertThat(call("PUT", "/api/v1/profile/preferences", Map.of("hideBalance", true), token).statusCode()).isEqualTo(400);
        for (String path : List.of("/api/v1/profile", "/api/v1/profile/preferences", "/api/v1/notifications",
                "/api/v1/notifications/unread-count", "/api/v1/notifications/aluguel")) {
            assertThat(call("GET", path, null, null).statusCode()).isEqualTo(401);
        }
        assertThat(call("PUT", "/api/v1/profile/preferences", Map.of(), null).statusCode()).isEqualTo(401);
        assertThat(call("PATCH", "/api/v1/notifications/aluguel/read", Map.of("read", true), null).statusCode()).isEqualTo(401);
        assertThat(call("POST", "/api/v1/notifications/read-all", null, null).statusCode()).isEqualTo(401);
    }

    @Test
    void notificationFiltersDetailsAndReadingAreConsistentAcrossSessions() throws Exception {
        String token = login(), second = login();
        call("PATCH", "/api/v1/notifications/aluguel/read", Map.of("read", false), token);
        call("PATCH", "/api/v1/notifications/pagamento/read", Map.of("read", false), token);
        var count = get("/api/v1/notifications/unread-count", token);
        assertThat(count.path("unreadCount").asLong()).isEqualTo(2);
        var detail = get("/api/v1/notifications/aluguel", token);
        assertThat(detail.path("read").asBoolean()).isFalse();
        assertThat(get("/api/v1/notifications/unread-count", token).path("unreadCount")).isEqualTo(count.path("unreadCount"));
        var account = get(detail.path("accountPath").asString(), token);
        assertThat(account.path("id").asString()).isEqualTo("aluguel");
        assertThat(account.path("dueDate").asString()).isEqualTo("2026-10-23");
        var filtered = get("/api/v1/notifications?category=financeira&read=false&size=1", token);
        assertThat(filtered.path("total").asLong()).isEqualTo(2);
        assertThat(filtered.path("totalPages").asInt()).isEqualTo(2);
        assertThat(filtered.path("items").size()).isEqualTo(1);
        assertThat(filtered.path("unreadCount").asLong()).isEqualTo(2);
        var first = call("PATCH", "/api/v1/notifications/aluguel/read", Map.of("read", true), token);
        assertThat(first.statusCode()).isEqualTo(200);
        var repeated = call("PATCH", "/api/v1/notifications/aluguel/read", Map.of("read", true), second);
        assertThat(json.readTree(repeated.body()).path("readAt")).isEqualTo(json.readTree(first.body()).path("readAt"));
        assertThat(get("/api/v1/notifications/unread-count", second).path("unreadCount").asLong()).isEqualTo(1);
        assertThat(json.readTree(call("POST", "/api/v1/notifications/read-all", null, token).body()).path("updatedCount").asInt()).isEqualTo(1);
        assertThat(json.readTree(call("POST", "/api/v1/notifications/read-all", null, token).body()).path("updatedCount").asInt()).isZero();
        assertThat(get("/api/v1/notifications?read=false", token).path("items").isEmpty()).isTrue();
        assertThat(get("/api/v1/notifications?q=aluguel", token).path("total").asLong()).isEqualTo(1);
        call("PATCH", "/api/v1/notifications/aluguel/read", Map.of("read", false), token);
        assertThat(get("/api/v1/notifications/aluguel", token).path("readAt").isNull()).isTrue();
        call("PATCH", "/api/v1/notifications/pagamento/read", Map.of("read", false), token);
    }

    @Test
    void notificationValidationAndSwaggerDescribeTheRealOperations() throws Exception {
        String token = login();
        for (String suffix : List.of("?size=101", "?page=-1", "?category=other", "?read=invalid", "?q=" + "a".repeat(161))) {
            assertThat(call("GET", "/api/v1/notifications" + suffix, null, token).statusCode()).isEqualTo(400);
        }
        assertThat(call("GET", "/api/v1/notifications/missing", null, token).statusCode()).isEqualTo(404);
        assertThat(call("PATCH", "/api/v1/notifications/missing/read", Map.of("read", true), token).statusCode()).isEqualTo(404);
        assertThat(call("PATCH", "/api/v1/notifications/aluguel/read", Map.of(), token).statusCode()).isEqualTo(400);
        var spec = get("/v3/api-docs", null);
        assertThat(spec.path("paths").path("/api/v1/profile").path("put").path("responses").has("200")).isTrue();
        assertThat(spec.path("paths").path("/api/v1/notifications/{id}/read").path("patch").path("security").isEmpty()).isFalse();
        assertThat(spec.path("paths").path("/api/v1/notifications/read-all").has("post")).isTrue();
    }
}
