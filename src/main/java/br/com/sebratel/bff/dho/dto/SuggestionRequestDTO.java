package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SuggestionRequestDTO(
    @NotBlank(message = "O título é obrigatório")
    String title,
    
    @NotBlank(message = "A descrição é obrigatória")
    String description,

    @Email(message = "E-mail inválido")
    @NotBlank(message = "O e-mail é obrigatório")
    String email
) {
}
