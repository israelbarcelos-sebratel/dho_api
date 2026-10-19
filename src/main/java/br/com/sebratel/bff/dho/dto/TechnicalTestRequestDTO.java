package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TechnicalTestRequestDTO(
    @NotBlank(message = "O parecer é obrigatório")
    @Size(min = 10, message = "O parecer deve ter no mínimo 10 caracteres")
    String reason
) {
}
