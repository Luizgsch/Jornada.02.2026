package br.com.cashmobile.movement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MovementInput(
        @NotNull @Schema(example = "entrada") Movement.MovementType type,
        @NotBlank @Size(max = 160) @Schema(example = "Receita demonstrativa") String description,
        @NotNull @Digits(integer = 12, fraction = 0) @DecimalMin("1") @DecimalMax("999999999999")
        @Schema(implementation = Long.class, type = "integer", format = "int64", example = "12345", description = "Valor positivo em centavos inteiros; R$ 123,45 = 12345.") BigDecimal valueInCents,
        @NotNull @Schema(example = "2026-10-28", description = "Data entre 1900-01-01 e 9999-12-31.") LocalDate date,
        @NotBlank @Pattern(regexp = "Receitas diversas|Despesas administrativas|Outros")
        @Schema(example = "Receitas diversas", allowableValues = {"Receitas diversas", "Despesas administrativas", "Outros"}) String category,
        @NotBlank @Pattern(regexp = "Conta principal") @Schema(example = "Conta principal") String account,
        @Size(max = 2000) @Schema(example = "Exemplo fictício, sem integração bancária.") String notes) {}
