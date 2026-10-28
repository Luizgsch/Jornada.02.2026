package br.com.cashmobile.movement;

import br.com.cashmobile.auth.DemoSessions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/movements", produces = "application/json")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Movimentações", description = "Registros temporários da conta autenticada. Reiniciar o backend apaga todos. Anexos vinculados disponíveis na tag Documentos.")
@ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "400", description = "Campos, filtros ou identificador inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
public class MovementController {
    private final MovementService service;
    public MovementController(MovementService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Cadastrar movimentação", description = "ID, proprietário e datas de auditoria são definidos pelo servidor. Valor em centavos inteiros.")
    @ApiResponse(responseCode = "201", description = "Registro criado; Location aponta para a consulta")
    @ApiResponse(responseCode = "503", description = "Limite de armazenamento temporário atingido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Movement> create(@AuthenticationPrincipal DemoSessions.Session session, @Valid @RequestBody MovementInput input) {
        var movement = service.create(session.user().email(), input);
        return ResponseEntity.created(URI.create("/api/v1/movements/" + movement.id())).body(movement);
    }

    @GetMapping
    @Operation(summary = "Listar e filtrar movimentações", description = "deleted=false retorna ativos; deleted=true retorna somente a lixeira. Datas inclusivas. Ordenação: data decrescente, criação decrescente e ID. Página começa em zero.")
    @ApiResponse(responseCode = "200", description = "Página de movimentações")
    public MovementService.Page list(@AuthenticationPrincipal DemoSessions.Session session,
            @RequestParam(defaultValue = "false") boolean deleted,
            @RequestParam(required = false) Movement.MovementType type,
            @RequestParam(required = false) @Size(max = 160) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(session.user().email(), deleted, type, q, from, to, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar movimentação ativa")
    @ApiResponse(responseCode = "200", description = "Movimentação encontrada")
    @ApiResponse(responseCode = "404", description = "Registro inexistente, de outra conta ou na lixeira", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public Movement get(@AuthenticationPrincipal DemoSessions.Session session, @PathVariable UUID id) {
        return service.get(session.user().email(), id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar movimentação ativa", description = "Substitui os campos editáveis; preserva ID e data de criação. A última gravação vence.")
    @ApiResponse(responseCode = "200", description = "Movimentação atualizada")
    @ApiResponse(responseCode = "404", description = "Movimentação não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Movimentação na lixeira", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public Movement update(@AuthenticationPrincipal DemoSessions.Session session, @PathVariable UUID id, @Valid @RequestBody MovementInput input) {
        return service.update(session.user().email(), id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Enviar movimentação para a lixeira", description = "Exclusão reversível. Repetir a exclusão retorna 204 sem alterar a data da primeira exclusão.")
    @ApiResponse(responseCode = "204", description = "Registro na lixeira")
    @ApiResponse(responseCode = "404", description = "Movimentação não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public void trash(@AuthenticationPrincipal DemoSessions.Session session, @PathVariable UUID id) {
        service.trash(session.user().email(), id);
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restaurar movimentação da lixeira")
    @ApiResponse(responseCode = "200", description = "Movimentação restaurada")
    @ApiResponse(responseCode = "404", description = "Movimentação não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Movimentação já ativa", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public Movement restore(@AuthenticationPrincipal DemoSessions.Session session, @PathVariable UUID id) {
        return service.restore(session.user().email(), id);
    }
}
