package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OpportunityApprovalDTO(
    @NotBlank(message = "O motivo é obrigatório")
    @Size(min = 200, message = "deve ter no mínimo 200 caracteres")
    String reason
) {
}
