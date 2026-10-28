package br.com.cashmobile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.profiles.active=")
class DocumentationDisabledTests {
    @LocalServerPort
    private int port;

    @Test
    void documentationIsNotPublishedByDefault() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            for (String path : new String[]{"/v3/api-docs", "/swagger-ui/index.html"}) {
                var response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .timeout(Duration.ofSeconds(20)).GET().build(), HttpResponse.BodyHandlers.ofString());
                assertThat(response.statusCode()).isEqualTo(404);
            }
        }
    }
}
