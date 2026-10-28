package br.com.cashmobile.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Conta pública de demonstração, sem banco. Sessões expiram e são perdidas ao reiniciar.")
public class AuthController {
    private final DemoSessions sessions;

    public AuthController(DemoSessions sessions) { this.sessions = sessions; }

    @PostMapping(value = "/login", produces = "application/json")
    @Operation(summary = "Entrar com a conta de demonstração",
            description = "Use demo@cash.local / Cash123!. Copie accessToken para Authorize. "
                    + "O token é opaco, não é JWT. Não use credenciais reais.")
    @ApiResponse(responseCode = "200", description = "Sessão criada")
    @ApiResponse(responseCode = "400", description = "Corpo inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "503", description = "Limite temporário de sessões atingido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<DemoSessions.Grant> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(sessions.login(request.email(), request.password()));
    }

    @GetMapping(value = "/me", produces = "application/json")
    @Operation(summary = "Consultar a sessão atual", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Usuário fictício e expiração")
    @ApiResponse(responseCode = "401", description = "Sessão ausente, inválida ou expirada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public DemoSessions.Session me(@AuthenticationPrincipal DemoSessions.Session session) { return session; }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerrar a sessão atual", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Token revogado imediatamente")
    @ApiResponse(responseCode = "401", description = "Sessão ausente, inválida ou expirada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public void logout(HttpServletRequest request) {
        sessions.logout(request.getHeader(HttpHeaders.AUTHORIZATION).substring(7));
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 254) @Schema(example = "demo@cash.local") String email,
            @NotBlank @Size(max = 72) @Schema(example = "Cash123!", accessMode = Schema.AccessMode.WRITE_ONLY) String password) {
        @Override public String toString() { return "LoginRequest[redacted]"; }
    }
}
