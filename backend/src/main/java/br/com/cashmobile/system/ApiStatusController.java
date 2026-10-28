package br.com.cashmobile.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/status")
@Tag(name = "Sistema", description = "Informações públicas sobre a demonstração")
public class ApiStatusController {
    @GetMapping
    @ApiResponse(responseCode = "200", description = "Estado da demonstração")
    @Operation(summary = "Consultar o estado da API demonstrativa",
            description = "Endpoint público, sem informações pessoais ou configurações internas. "
                    + "Para disponibilidade operacional, use GET /actuator/health.")
    public ApiStatus status() {
        return new ApiStatus("CASH Mobile", "v1", "demonstracao", true, "memoria");
    }

    public record ApiStatus(String application, String apiVersion, String mode,
                            boolean authenticationEnabled, String storage) {}
}
