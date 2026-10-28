package br.com.cashmobile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class ApiDocumentationTests {
    @LocalServerPort
    private int port;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();
    private final JsonMapper json = JsonMapper.builder().build();

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(20)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void documentationDescribesTheRealStatusEndpoint() throws Exception {
        var status = get("/api/v1/status");
        assertThat(status.statusCode()).isEqualTo(200);
        var data = json.readTree(status.body());
        assertThat(data.path("application").asString()).isEqualTo("CASH Mobile");
        assertThat(data.path("authenticationEnabled").asBoolean()).isTrue();
        assertThat(data.path("storage").asString()).isEqualTo("memoria");
        var response = get("/v3/api-docs");
        assertThat(response.statusCode()).isEqualTo(200);
        var spec = json.readTree(response.body());
        assertThat(spec.path("info").path("title").asString()).contains("CASH Mobile");
        assertThat(spec.path("paths").has("/api/v1/status")).isTrue();
        assertThat(spec.path("paths").has("/actuator/health")).isFalse();
        assertThat(get("/swagger-ui/index.html").body()).contains("swagger-ui-bundle.js");
        assertThat(get("/actuator/health").statusCode()).isEqualTo(200);
    }

    @Test
    void corsAllowsTheFrontendAndRejectsAnUnlistedOrigin() throws Exception {
        for (String origin : new String[]{"http://localhost:3000", "https://localhost", "https://unlisted.invalid"}) {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/status"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Origin", origin)
                    .header("Access-Control-Request-Method", "GET")
                    .header("Access-Control-Request-Headers", "Authorization")
                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (origin.endsWith(".invalid")) {
                assertThat(response.statusCode()).isEqualTo(403);
                assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
            } else {
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).contains(origin);
            }
        }
    }
}
