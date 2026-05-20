package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OpportunityApprovalDTO(
    @NotNull(message = "A data da oportunidade é obrigatória")
    @FutureOrPresent(message = "A data da oportunidade deve ser hoje ou uma data futura")
    LocalDateTime opportunityDate,

    @NotBlank(message = "O motivo é obrigatório")
    @Size(min = 200, message = "deve ter no mínimo 200 caracteres")
    String reason
) {
}
