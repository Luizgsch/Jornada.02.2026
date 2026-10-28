package br.com.cashmobile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class MovementApiTests {
    @LocalServerPort private int port;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();
    private final String base = "/api/v1/movements";

    private HttpResponse<String> call(String method, String path, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(20));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        if (body != null) builder.header("Content-Type", "application/json");
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login() throws Exception {
        var response = call("POST", "/api/v1/auth/login", Map.of("email", "demo@cash.local", "password", "Cash123!"), null);
        assertThat(response.statusCode()).isEqualTo(200);
        return json.readTree(response.body()).path("accessToken").asString();
    }

    private Map<String, Object> input(String description) {
        return new LinkedHashMap<>(Map.of("type", "entrada", "description", description, "valueInCents", 12345,
                "date", "2026-10-28", "category", "Receitas diversas", "account", "Conta principal", "notes", "Fictício"));
    }

    private JsonNode create(String token, Map<String, Object> body) throws Exception {
        var response = call("POST", base, body, token);
        assertThat(response.statusCode()).isEqualTo(201);
        var movement = json.readTree(response.body());
        assertThat(response.headers().firstValue("Location")).contains(base + "/" + movement.path("id").asString());
        return movement;
    }

    @Test
    void createReadEditTrashAndRestorePreserveIdentityAndCreation() throws Exception {
        String token = login();
        String marker = "cycle-" + UUID.randomUUID();
        var body = input(marker);
        body.put("owner", "another-account");
        body.put("id", "untrusted-id");
        var created = create(token, body);
        String id = created.path("id").asString();
        assertThat(id).isNotEqualTo("untrusted-id");
        assertThat(created.has("owner")).isFalse();
        assertThat(call("GET", base + "/" + id, null, token).statusCode()).isEqualTo(200);
        body.put("description", marker + " edited");
        body.put("type", "saida");
        body.put("valueInCents", 56789);
        var update = call("PUT", base + "/" + id, body, token);
        assertThat(update.statusCode()).isEqualTo(200);
        var edited = json.readTree(update.body());
        assertThat(edited.path("createdAt")).isEqualTo(created.path("createdAt"));
        assertThat(edited.path("valueInCents").asLong()).isEqualTo(56789);
        assertThat(call("DELETE", base + "/" + id, null, token).statusCode()).isEqualTo(204);
        assertThat(call("DELETE", base + "/" + id, null, token).statusCode()).isEqualTo(204);
        assertThat(call("GET", base + "/" + id, null, token).statusCode()).isEqualTo(404);
        assertThat(call("PUT", base + "/" + id, body, token).statusCode()).isEqualTo(409);
        var active = json.readTree(call("GET", base + "?q=" + marker, null, token).body());
        assertThat(active.path("total").asLong()).isZero();
        var trash = json.readTree(call("GET", base + "?deleted=true&q=" + marker, null, token).body());
        assertThat(trash.path("total").asLong()).isEqualTo(1);
        assertThat(trash.path("items").get(0).path("deletedAt").isNull()).isFalse();
        var restore = call("POST", base + "/" + id + "/restore", null, token);
        assertThat(restore.statusCode()).isEqualTo(200);
        assertThat(json.readTree(restore.body()).path("deletedAt").isNull()).isTrue();
        assertThat(call("POST", base + "/" + id + "/restore", null, token).statusCode()).isEqualTo(409);
        assertThat(json.readTree(call("GET", base + "?q=" + marker, null, token).body()).path("total").asLong()).isEqualTo(1);
    }

    @Test
    void filtersAreInclusiveAndPaginationDoesNotLeakOtherResults() throws Exception {
        String token = login();
        String marker = "filter-" + UUID.randomUUID();
        var early = input(marker + " early"); early.put("date", "2026-10-01");
        var late = input(marker + " late"); late.put("type", "saida"); late.put("date", "2026-10-31");
        create(token, early); create(token, late);
        String query = "?q=" + marker + "&from=2026-10-01&to=2026-10-31&size=1";
        var first = json.readTree(call("GET", base + query, null, token).body());
        assertThat(first.path("total").asLong()).isEqualTo(2);
        assertThat(first.path("totalPages").asInt()).isEqualTo(2);
        assertThat(first.path("items").get(0).path("date").asString()).isEqualTo("2026-10-31");
        var next = json.readTree(call("GET", base + query + "&page=1", null, token).body());
        assertThat(next.path("items").get(0).path("date").asString()).isEqualTo("2026-10-01");
        var entry = json.readTree(call("GET", base + "?q=" + marker + "&type=entrada", null, token).body());
        assertThat(entry.path("total").asLong()).isEqualTo(1);
        var beyond = json.readTree(call("GET", base + query + "&page=2147483647", null, token).body());
        assertThat(beyond.path("items").isEmpty()).isTrue();
    }

    @Test
    void invalidMoneyFieldsFiltersAndIdsReturnClientErrors() throws Exception {
        String token = login();
        for (Map<String, Object> invalid : java.util.List.of(
                Map.<String,Object>of("valueInCents", 0), Map.<String,Object>of("valueInCents", -1),
                Map.<String,Object>of("valueInCents", 1.5), Map.<String,Object>of("valueInCents", 1000000000000L),
                Map.<String,Object>of("type", "other"), Map.<String,Object>of("description", "   "),
                Map.<String,Object>of("date", "2026-02-30"), Map.<String,Object>of("date", "1899-12-31"),
                Map.<String,Object>of("category", "Unknown"), Map.<String,Object>of("account", "Unknown"),
                Map.<String,Object>of("notes", "a".repeat(2001)))) {
            var body = input("Invalid"); body.putAll(invalid);
            var response = call("POST", base, body, token);
            assertThat(response.statusCode()).as("Invalid input %s", invalid.keySet()).isEqualTo(400);
            assertThat(response.body()).doesNotContain("stackTrace");
        }
        assertThat(call("POST", base, Map.of(), token).statusCode()).isEqualTo(400);
        for (String query : new String[]{"?page=-1", "?size=0", "?size=101", "?type=other",
                "?q=" + "a".repeat(161), "?from=2026-10-31&to=2026-10-01"}) {
            assertThat(call("GET", base + query, null, token).statusCode()).as(query).isEqualTo(400);
        }
        assertThat(call("GET", base + "/invalid-id", null, token).statusCode()).isEqualTo(400);
        assertThat(call("GET", base + "/" + UUID.randomUUID(), null, token).statusCode()).isEqualTo(404);
    }

    @Test
    void everyOperationRequiresAuthenticationAndSwaggerDeclaresTheContract() throws Exception {
        String id = UUID.randomUUID().toString();
        for (String method : new String[]{"GET", "POST", "PUT", "DELETE"}) {
            assertThat(call(method, method.equals("POST") ? base : base + "/" + id,
                    method.equals("POST") || method.equals("PUT") ? input("Unauthorized") : null, null).statusCode()).isEqualTo(401);
        }
        assertThat(call("POST", base + "/" + id + "/restore", null, null).statusCode()).isEqualTo(401);
        var spec = json.readTree(call("GET", "/v3/api-docs", null, null).body());
        assertThat(spec.path("paths").path(base).path("post").path("responses").has("201")).isTrue();
        assertThat(spec.path("paths").path(base).path("get").path("security").isEmpty()).isFalse();
        assertThat(spec.path("paths").has(base + "/{id}/restore")).isTrue();
    }
}
