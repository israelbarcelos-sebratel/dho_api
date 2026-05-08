package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.Size;

public record OpportunityApprovalDTO(
    @Size(min = 200, message = "A justificativa deve ter no mínimo 200 caracteres")
    String justification
) {
}
