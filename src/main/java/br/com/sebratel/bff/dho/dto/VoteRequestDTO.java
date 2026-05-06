package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VoteRequestDTO(
    @Email(message = "E-mail inválido")
    @NotBlank(message = "O e-mail é obrigatório")
    String email,

    @NotNull(message = "O valor do voto é obrigatório")
    @Min(value = -1, message = "O voto deve ser -1 ou 1")
    @Max(value = 1, message = "O voto deve ser -1 ou 1")
    Integer vote
) {
}
