package br.com.cashmobile.auth;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

public final class AuthErrors {
    private AuthErrors() {}

    public static void write(JsonMapper json, HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        if (status == 401) response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        json.writeValue(response.getOutputStream(), Map.of(
                "type", "about:blank", "status", status,
                "title", status == 401 ? "Não autorizado" : "Acesso negado",
                "detail", status == 401 ? "Credenciais ou sessão inválidas. Faça login novamente."
                        : "Sua sessão não tem permissão para esta operação."));
    }
}
