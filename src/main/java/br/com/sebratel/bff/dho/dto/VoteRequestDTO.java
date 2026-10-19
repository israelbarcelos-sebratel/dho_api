package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record VoteRequestDTO(
    @NotNull(message = "O valor do voto é obrigatório")
    @Min(value = -1, message = "O voto deve ser -1 ou 1")
    @Max(value = 1, message = "O voto deve ser -1 ou 1")
    Integer vote
) {
}
