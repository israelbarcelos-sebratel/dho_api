package br.com.sebratel.bff.dho.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atribuir um recrutador responsável")
public record OpportunityAssignRecruiterDTO(
    @Schema(description = "ID do recrutador responsável", example = "10")
    @NotNull(message = "O ID do recrutador é obrigatório")
    Integer recruiterId
) {
}
