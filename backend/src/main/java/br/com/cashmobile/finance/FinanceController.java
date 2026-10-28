package br.com.cashmobile.finance;

import br.com.cashmobile.auth.DemoSessions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/finance", produces = "application/json")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Financeiro", description = "Consultas demonstrativas. Catálogo com posição fixa em 22/10/2026; não executa pagamentos nem baixas automáticas.")
@ApiResponse(responseCode = "400", description = "Filtros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "401", description = "Sessão inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
public class FinanceController {
    private final FinanceService service;
    public FinanceController(FinanceService service) { this.service = service; }

    @GetMapping("/summary")
    @Operation(summary = "Consultar indicadores e gráfico de seis meses", description = "Saldo inicial zero mais entradas menos saídas ativas de todos os meses. Fluxos mensais e gráfico vêm das movimentações da conta. Contas fictícias não alteram o saldo.")
    @ApiResponse(responseCode = "200", description = "Indicadores e série mensal em centavos")
    public FinanceService.Summary summary(@AuthenticationPrincipal DemoSessions.Session session,
            @RequestParam(defaultValue = "2026-10") String month) { return service.summary(session.user().email(), month); }

    @GetMapping("/accounts")
    @Operation(summary = "Consultar contas fictícias a pagar ou receber", description = "Somente consulta. status=open exclui vencidas; use all para todas. Total em centavos corresponde ao filtro completo, antes da paginação.")
    @ApiResponse(responseCode = "200", description = "Página de contas fictícias")
    public FinanceService.AccountPage accounts(@RequestParam(defaultValue = "2026-10") String month,
            @RequestParam(defaultValue = "payable") FinanceService.Kind kind,
            @RequestParam(defaultValue = "all") FinanceService.Status status,
            @RequestParam(required = false) @Size(max = 160) String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.accounts(month, kind, status, q, page, size);
    }

    @GetMapping("/accounts/{id}")
    @Operation(summary = "Ver detalhes de uma conta fictícia")
    @ApiResponse(responseCode = "200", description = "Conta fictícia encontrada")
    @ApiResponse(responseCode = "404", description = "Conta não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public FinanceService.Account account(@PathVariable String id) { return service.account(id); }

    @GetMapping("/revenue")
    @Operation(summary = "Consultar receitas fictícias por setor e funcionário", description = "Catálogo independente das entradas cadastradas. Exemplos somente em outubro de 2026.")
    @ApiResponse(responseCode = "200", description = "Catálogo de receitas fictícias")
    public FinanceService.RevenueReport revenue(@RequestParam(defaultValue = "2026-10") String month) { return service.revenue(month); }

    @GetMapping("/budget")
    @Operation(summary = "Consultar orçamento fictício", description = "Previsto e realizado são exemplos independentes das movimentações. Exemplos somente em outubro de 2026.")
    @ApiResponse(responseCode = "200", description = "Catálogo de orçamento fictício")
    public FinanceService.BudgetReport budget(@RequestParam(defaultValue = "2026-10") String month) { return service.budget(month); }
}
