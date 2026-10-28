package br.com.cashmobile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class AuthenticationTests {
    @LocalServerPort private int port;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(20));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        if (body != null) builder.header("Content-Type", "application/json");
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login() throws Exception {
        var response = request("POST", "/api/v1/auth/login",
                "{\"email\":\"demo@cash.local\",\"password\":\"Cash123!\"}", null);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Cache-Control")).contains("no-store");
        assertThat(response.headers().firstValue("Set-Cookie")).isEmpty();
        var grant = json.readTree(response.body());
        assertThat(grant.path("tokenType").asString()).isEqualTo("Bearer");
        assertThat(Instant.parse(grant.path("expiresAt").asString())).isAfter(Instant.now());
        assertThat(grant.path("user").path("role").asString()).isEqualTo("DEMO");
        assertThat(response.body()).doesNotContain("Cash123!", "password");
        return grant.path("accessToken").asString();
    }

    @Test
    void loginSessionAndLogoutRevokeOnlyTheCurrentToken() throws Exception {
        String first = login();
        String second = login();
        assertThat(first).matches("[A-Za-z0-9_-]{43}").isNotEqualTo(second);
        var me = request("GET", "/api/v1/auth/me", null, first);
        assertThat(me.statusCode()).isEqualTo(200);
        assertThat(json.readTree(me.body()).path("user").path("email").asString()).isEqualTo("demo@cash.local");
        assertThat(me.body()).doesNotContain("accessToken", "password");
        var logout = request("POST", "/api/v1/auth/logout", null, first);
        assertThat(logout.statusCode()).isEqualTo(204);
        assertThat(logout.body()).isEmpty();
        assertThat(request("GET", "/api/v1/auth/me", null, first).statusCode()).isEqualTo(401);
        assertThat(request("POST", "/api/v1/auth/logout", null, first).statusCode()).isEqualTo(401);
        assertThat(request("GET", "/api/v1/auth/me", null, second).statusCode()).isEqualTo(200);
        request("POST", "/api/v1/auth/logout", null, second);
    }

    @Test
    void credentialsAreCheckedWithoutRevealingWhichFieldFailed() throws Exception {
        var wrongPassword = request("POST", "/api/v1/auth/login",
                "{\"email\":\"demo@cash.local\",\"password\":\"wrong\"}", null);
        var wrongEmail = request("POST", "/api/v1/auth/login",
                "{\"email\":\"unknown@cash.local\",\"password\":\"Cash123!\"}", null);
        assertThat(wrongPassword.statusCode()).isEqualTo(401);
        assertThat(wrongEmail.statusCode()).isEqualTo(401);
        assertThat(json.readTree(wrongPassword.body()).path("detail"))
                .isEqualTo(json.readTree(wrongEmail.body()).path("detail"));
        assertThat(wrongPassword.headers().firstValue("WWW-Authenticate")).contains("Bearer");
        assertThat(wrongPassword.body()).doesNotContain("wrong", "Cash123!", "stackTrace");
    }

    @Test
    void invalidPayloadsAndUnauthenticatedAccessAreRejected() throws Exception {
        assertThat(request("POST", "/api/v1/auth/login", "{}", null).statusCode()).isEqualTo(400);
        assertThat(request("POST", "/api/v1/auth/login", "{", null).statusCode()).isEqualTo(400);
        for (String path : new String[]{"/api/v1/auth/me", "/api/v1/movements"}) {
            assertThat(request("GET", path, null, null).statusCode()).isEqualTo(401);
            assertThat(request("GET", path, null, "invalid").statusCode()).isEqualTo(401);
            assertThat(request("GET", path, null, "A".repeat(43)).statusCode()).isEqualTo(401);
        }
        var cookieOnly = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/me"))
                .header("Cookie", "JSESSIONID=fake").GET().build(), HttpResponse.BodyHandlers.ofString());
        assertThat(cookieOnly.statusCode()).isEqualTo(401);
        assertThat(cookieOnly.headers().firstValue("Location")).isEmpty();
    }

    @Test
    void swaggerDeclaresBearerOnlyForProtectedOperations() throws Exception {
        var spec = json.readTree(request("GET", "/v3/api-docs", null, null).body());
        assertThat(spec.path("components").path("securitySchemes").path("bearerAuth").path("scheme").asString())
                .isEqualTo("bearer");
        assertThat(spec.path("paths").path("/api/v1/auth/me").path("get").path("security").isEmpty()).isFalse();
        assertThat(spec.path("paths").path("/api/v1/auth/login").path("post").has("security")).isFalse();
        assertThat(spec.path("paths").path("/api/v1/auth/login").path("post")
                .path("responses").path("401").path("content").has("application/problem+json")).isTrue();
    }
}
