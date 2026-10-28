package br.com.cashmobile;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
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
class FinanceAndDocumentApiTests {
    @LocalServerPort private int port;
    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();
    private HttpRequest.Builder request(String path, String token) {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(30));
        if (token != null) request.header("Authorization", "Bearer " + token);
        return request;
    }
    private HttpResponse<String> call(String method, String path, Object body, String token) throws Exception {
        var request = request(path, token);
        if (body != null) request.header("Content-Type", "application/json");
        return client.send(request.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(), HttpResponse.BodyHandlers.ofString());
    }
    private String login() throws Exception {
        return json.readTree(call("POST", "/api/v1/auth/login", Map.of("email", "demo@cash.local", "password", "Cash123!"), null)
                .body()).path("accessToken").asString();
    }
    private Map<String,Object> input(String marker, String type, int cents, String date) {
        return new LinkedHashMap<>(Map.of("type", type, "description", marker, "valueInCents", cents,
                "date", date, "category", "Outros", "account", "Conta principal"));
    }
    private String movement(String token, Map<String,Object> body) throws Exception {
        var response = call("POST", "/api/v1/movements", body, token);
        assertThat(response.statusCode()).isEqualTo(201);
        return json.readTree(response.body()).path("id").asString();
    }
    private JsonNode get(String path, String token) throws Exception {
        var response = call("GET", path, null, token);
        assertThat(response.statusCode()).isEqualTo(200);
        return json.readTree(response.body());
    }
    private HttpResponse<byte[]> upload(String token, String id, String filename, String mime, byte[] bytes) throws Exception {
        String boundary = "cash-" + UUID.randomUUID();
        var body = new ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + filename
                + "\"\r\nContent-Type: " + mime + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(bytes); body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return client.send(request("/api/v1/movements/" + id + "/attachment", token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    @Test
    void summaryRecalculatesForEditsTrashAndRestoreWithoutMixingCatalogs() throws Exception {
        String token = login();
        String summary = "/api/v1/finance/summary?month=2099-11";
        var before = get(summary, token);
        String incoming = movement(token, input("summary-in", "entrada", 12345, "2099-11-01"));
        String outgoing = movement(token, input("summary-out", "saida", 2345, "2099-11-30"));
        var after = get(summary, token);
        assertThat(after.path("incomingCents").asLong()).isEqualTo(before.path("incomingCents").asLong() + 12345);
        assertThat(after.path("outgoingCents").asLong()).isEqualTo(before.path("outgoingCents").asLong() + 2345);
        assertThat(after.path("balanceCents").asLong()).isEqualTo(before.path("balanceCents").asLong() + 10000);
        assertThat(after.path("movedCents").asLong()).isEqualTo(before.path("movedCents").asLong() + 14690);
        assertThat(after.path("chart").size()).isEqualTo(6);
        assertThat(after.path("chart").get(5).path("month").asString()).isEqualTo("2099-11");
        assertThat(after.path("payableOpenCents").asLong()).isZero();
        assertThat(call("PUT", "/api/v1/movements/" + incoming, input("summary-in-edited", "entrada", 20000, "2099-11-01"), token).statusCode()).isEqualTo(200);
        assertThat(get(summary, token).path("balanceCents").asLong()).isEqualTo(before.path("balanceCents").asLong() + 17655);
        call("DELETE", "/api/v1/movements/" + outgoing, null, token);
        assertThat(get(summary, token).path("balanceCents").asLong()).isEqualTo(before.path("balanceCents").asLong() + 20000);
        call("POST", "/api/v1/movements/" + outgoing + "/restore", null, token);
        assertThat(get(summary, token).path("balanceCents").asLong()).isEqualTo(before.path("balanceCents").asLong() + 17655);
        call("DELETE", "/api/v1/movements/" + incoming, null, token);
        call("DELETE", "/api/v1/movements/" + outgoing, null, token);
        assertThat(get(summary, token).path("balanceCents")).isEqualTo(before.path("balanceCents"));
    }

    @Test
    void readonlyCatalogMatchesTheAppAndRejectsInvalidFilters() throws Exception {
        String token = login();
        var summary = get("/api/v1/finance/summary", token);
        assertThat(summary.path("catalogReferenceDate").asString()).isEqualTo("2026-10-22");
        assertThat(summary.path("payableOpenCents").asLong()).isEqualTo(524000);
        assertThat(summary.path("receivableOpenCents").asLong()).isEqualTo(2650000);
        assertThat(summary.path("pendingCount").asLong()).isEqualTo(3);
        assertThat(summary.path("overdueCount").asLong()).isEqualTo(1);
        var overdue = get("/api/v1/finance/accounts?status=overdue&size=1", token);
        assertThat(overdue.path("total").asLong()).isEqualTo(1);
        assertThat(overdue.path("totalCents").asLong()).isEqualTo(68000);
        assertThat(get("/api/v1/finance/accounts?kind=receivable&size=1", token).path("totalCents").asLong()).isEqualTo(2650000);
        assertThat(get("/api/v1/finance/accounts/software", token).path("status").asString()).isEqualTo("paid");
        assertThat(get("/api/v1/finance/revenue", token).path("totalCents").asLong()).isEqualTo(2650000);
        assertThat(get("/api/v1/finance/revenue?month=2099-01", token).path("items").isEmpty()).isTrue();
        assertThat(get("/api/v1/finance/budget", token).path("availableCents").asLong()).isEqualTo(17000);
        for (String path : List.of("/api/v1/finance/summary?month=bad", "/api/v1/finance/summary?month=1899-12",
                "/api/v1/finance/accounts?size=101", "/api/v1/finance/accounts?status=other")) {
            assertThat(call("GET", path, null, token).statusCode()).isEqualTo(400);
        }
        assertThat(call("GET", "/api/v1/finance/accounts/missing", null, token).statusCode()).isEqualTo(404);
        assertThat(call("POST", "/api/v1/finance/accounts/software/pay", Map.of(), token).statusCode()).isEqualTo(404);
    }

    @Test
    void attachmentReplacementDownloadTrashAndRestoreKeepTheLink() throws Exception {
        String token = login(), marker = "document-" + UUID.randomUUID();
        String id = movement(token, input(marker, "entrada", 170, "2026-10-28"));
        String path = "/api/v1/movements/" + id + "/attachment";
        byte[] pdf = "%PDF-1.4\n% Ficticio de teste\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        var uploaded = upload(token, id, "../prova.pdf", "application/pdf", pdf);
        assertThat(uploaded.statusCode()).isEqualTo(200);
        assertThat(json.readTree(uploaded.body()).path("name").asString()).isEqualTo("prova.pdf");
        var download = client.send(request(path, token).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        assertThat(download.statusCode()).isEqualTo(200);
        assertThat(download.body()).isEqualTo(pdf);
        assertThat(download.headers().firstValue("Content-Type")).contains("application/pdf");
        assertThat(download.headers().firstValue("Content-Disposition").orElseThrow()).contains("attachment").doesNotContain("../");
        assertThat(get("/api/v1/documents?q=" + marker + "&kind=pdf&month=2026-10", token).path("total").asLong()).isEqualTo(1);
        assertThat(get("/api/v1/documents?q=" + marker + "&kind=image", token).path("total").asLong()).isZero();
        byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a9VEAAAAASUVORK5CYII=");
        assertThat(upload(token, id, "prova.exe", "image/png", png).statusCode()).isEqualTo(200);
        assertThat(get("/api/v1/documents?q=" + marker + "&kind=image", token).path("items").get(0).path("name").asString()).isEqualTo("prova.png");
        call("DELETE", "/api/v1/movements/" + id, null, token);
        assertThat(call("GET", path, null, token).statusCode()).isEqualTo(404);
        assertThat(get("/api/v1/documents?q=" + marker, token).path("total").asLong()).isZero();
        assertThat(upload(token, id, "prova.pdf", "application/pdf", pdf).statusCode()).isEqualTo(404);
        call("POST", "/api/v1/movements/" + id + "/restore", null, token);
        assertThat(client.send(request(path, token).GET().build(), HttpResponse.BodyHandlers.ofByteArray()).body()).isEqualTo(png);
        assertThat(call("DELETE", path, null, token).statusCode()).isEqualTo(204);
        assertThat(call("DELETE", path, null, token).statusCode()).isEqualTo(204);
        assertThat(call("GET", path, null, token).statusCode()).isEqualTo(404);
        assertThat(call("GET", "/api/v1/movements/" + id, null, token).statusCode()).isEqualTo(200);
        call("DELETE", "/api/v1/movements/" + id, null, token);
    }

    @Test
    void fileValidationAndAuthenticationProtectEveryRoute() throws Exception {
        String token = login();
        String id = movement(token, input("invalid-doc", "entrada", 100, "2026-10-28"));
        assertThat(upload(token, id, "empty.pdf", "application/pdf", new byte[0]).statusCode()).isEqualTo(400);
        assertThat(upload(token, id, "bad.pdf", "application/pdf", "not pdf".getBytes(StandardCharsets.US_ASCII)).statusCode()).isEqualTo(400);
        assertThat(upload(token, id, "bad.txt", "text/plain", new byte[]{1,2}).statusCode()).isEqualTo(415);
        assertThat(upload(token, id, "large.pdf", "application/pdf", new byte[5 * 1024 * 1024 + 1]).statusCode()).isEqualTo(413);
        var jpeg = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB), "jpg", jpeg);
        assertThat(upload(token, id, "imagem.jpeg", "image/jpeg", jpeg.toByteArray()).statusCode()).isEqualTo(200);
        assertThat(get("/api/v1/documents?q=invalid-doc&kind=image", token).path("items").get(0).path("contentType").asString())
                .isEqualTo("image/jpeg");
        for (String path : List.of("/api/v1/finance/summary", "/api/v1/finance/accounts", "/api/v1/finance/accounts/energia",
                "/api/v1/finance/revenue", "/api/v1/finance/budget", "/api/v1/documents", "/api/v1/movements/" + id + "/attachment")) {
            assertThat(call("GET", path, null, null).statusCode()).isEqualTo(401);
        }
        assertThat(upload(null, id, "a.pdf", "application/pdf", new byte[]{1}).statusCode()).isEqualTo(401);
        assertThat(call("DELETE", "/api/v1/movements/" + id + "/attachment", null, null).statusCode()).isEqualTo(401);
        assertThat(call("GET", "/api/v1/documents?month=bad", null, token).statusCode()).isEqualTo(400);
        var spec = get("/v3/api-docs", null);
        assertThat(spec.path("paths").path("/api/v1/movements/{id}/attachment").path("put")
                .path("requestBody").path("content").has("multipart/form-data")).isTrue();
        assertThat(spec.path("paths").path("/api/v1/finance/summary").path("get").path("security").isEmpty()).isFalse();
        assertThat(spec.path("components").path("schemas").path("MovementInput").path("properties")
                .path("valueInCents").path("type").asString()).isEqualTo("integer");
        assertThat(spec.path("paths").path("/api/v1/movements/{id}/attachment").path("put")
                .path("responses").path("200").path("content").has("application/json")).isTrue();
        assertThat(spec.path("paths").path("/api/v1/finance/summary").path("get").path("responses").has("200")).isTrue();
        call("DELETE", "/api/v1/movements/" + id, null, token);
    }
}
