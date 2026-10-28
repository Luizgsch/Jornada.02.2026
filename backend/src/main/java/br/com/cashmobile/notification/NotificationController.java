package br.com.cashmobile.notification;

import br.com.cashmobile.auth.DemoSessions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/notifications", produces = "application/json")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notificações", description = "Quatro exemplos fixos com referência em 22/10/2026. Somente controle de leitura, sem push, e-mail ou eventos automáticos.")
@ApiResponse(responseCode = "400", description = "Campos ou filtros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "401", description = "Sessão inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Listar e filtrar notificações", description = "unreadCount conta todas as não lidas, independentemente do filtro e da página. Ordenação por receivedAt decrescente e ID.")
    @ApiResponse(responseCode = "200", description = "Página de alertas fictícios")
    public NotificationService.NotificationPage list(@AuthenticationPrincipal DemoSessions.Session session,
            @RequestParam(required = false) DemoNotification.Category category,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) @Size(max = 160) String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(session.user().email(), category, read, q, page, size);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Consultar quantidade de não lidas")
    @ApiResponse(responseCode = "200", description = "Contagem da conta autenticada")
    public NotificationService.UnreadCount count(@AuthenticationPrincipal DemoSessions.Session session) { return service.count(session.user().email()); }

    @GetMapping("/{id}")
    @Operation(summary = "Ver detalhe de notificação", description = "Consultar não marca como lida. Use PATCH explicitamente. Links de conta correspondem ao catálogo de outubro de 2026.")
    @ApiResponse(responseCode = "200", description = "Alerta fictício encontrado")
    @ApiResponse(responseCode = "404", description = "Notificação não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public NotificationService.NotificationView get(@AuthenticationPrincipal DemoSessions.Session session, @PathVariable String id) { return service.get(session.user().email(), id); }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar como lida ou não lida", description = "Enviar read=true ou false. Repetir true preserva a primeira data de leitura; false limpa readAt.")
    @ApiResponse(responseCode = "200", description = "Estado de leitura atualizado")
    @ApiResponse(responseCode = "404", description = "Notificação não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public NotificationService.NotificationView read(@AuthenticationPrincipal DemoSessions.Session session, @PathVariable String id,
            @Valid @RequestBody ReadingInput input) { return service.read(session.user().email(), id, input.read()); }

    @PostMapping("/read-all")
    @Operation(summary = "Marcar todas como lidas", description = "Retorna quantas mudaram nesta operação. Repetir retorna zero alterações.")
    @ApiResponse(responseCode = "200", description = "Estado atualizado atomicamente")
    public NotificationService.ReadAllResult readAll(@AuthenticationPrincipal DemoSessions.Session session) { return service.readAll(session.user().email()); }

    public record ReadingInput(@NotNull @Schema(example = "true") Boolean read) {}
}
