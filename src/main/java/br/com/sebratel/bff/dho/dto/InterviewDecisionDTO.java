package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InterviewDecisionDTO(
    @NotBlank(message = "O motivo é obrigatório")
    @Size(min = 50, message = "deve ter no mínimo 50 caracteres")
    String reason
) {
}
