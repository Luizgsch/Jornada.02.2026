package br.com.cashmobile.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record ProfileInput(
        @NotBlank @Size(max = 100) @Schema(example = "Fulano de Lima") String name,
        @NotBlank @Email @Size(max = 254) @Schema(example = "fulano@example.com", description = "Contato fictício; não modifica o login.") String email,
        @Size(max = 25) @Pattern(regexp = "[+0-9() .-]*") @Schema(example = "0000-0000", description = "Opcional; use somente dados fictícios, ou omita/deixe vazio.") String phone,
        @NotBlank @Size(max = 120) @Schema(example = "Cartório de demonstração") String office) {}
