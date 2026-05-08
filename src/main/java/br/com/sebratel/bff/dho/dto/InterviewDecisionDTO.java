package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.Size;

public record InterviewDecisionDTO(
    @Size(min = 200, message = "O relato detalhado deve ter no mínimo 200 caracteres")
    String report
) {
}
