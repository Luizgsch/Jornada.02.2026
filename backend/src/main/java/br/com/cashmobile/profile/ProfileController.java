package br.com.cashmobile.profile;

import br.com.cashmobile.auth.DemoSessions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/profile", produces = "application/json")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Perfil", description = "Dados fictícios e preferências da conta autenticada. Não altera e-mail de login, senha ou permissão.")
@ApiResponse(responseCode = "401", description = "Sessão inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "400", description = "Campos inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
public class ProfileController {
    private final ProfileService service;
    public ProfileController(ProfileService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Consultar perfil demonstrativo", description = "email é contato editável; loginEmail e role são definidos pela sessão e permanecem imutáveis.")
    @ApiResponse(responseCode = "200", description = "Perfil fictício da conta")
    public ProfileService.ProfileView get(@AuthenticationPrincipal DemoSessions.Session session) { return service.get(session.user().email()); }

    @PutMapping
    @Operation(summary = "Editar dados do perfil", description = "Substitui nome, contato, telefone opcional e cartório. Preserva preferências e credenciais. Use dados fictícios.")
    @ApiResponse(responseCode = "200", description = "Perfil atualizado em memória")
    public ProfileService.ProfileView update(@AuthenticationPrincipal DemoSessions.Session session, @Valid @RequestBody ProfileInput input) {
        return service.update(session.user().email(), input);
    }

    @GetMapping("/preferences")
    @Operation(summary = "Consultar preferências")
    @ApiResponse(responseCode = "200", description = "Preferências atuais")
    public ProfileService.Preferences preferences(@AuthenticationPrincipal DemoSessions.Session session) { return service.preferences(session.user().email()); }

    @PutMapping("/preferences")
    @Operation(summary = "Salvar preferências", description = "Substitui ambas as opções, preservando os dados de perfil. São aplicadas pelo frontend somente após a integração.")
    @ApiResponse(responseCode = "200", description = "Preferências salvas em memória")
    public ProfileService.Preferences preferences(@AuthenticationPrincipal DemoSessions.Session session, @Valid @RequestBody PreferencesInput input) {
        return service.preferences(session.user().email(), input.hideBalance(), input.reduceMotion());
    }

    public record PreferencesInput(@NotNull @Schema(example = "true") Boolean hideBalance,
            @NotNull @Schema(example = "false") Boolean reduceMotion) {}
}
