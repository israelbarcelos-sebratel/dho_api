package br.com.sebratel.bff.dho.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados simplificados para seleção de pessoas do banco de talentos")
public record TalentPoolSelectResponseDTO(
    @Schema(description = "ID da pessoa (tabela people)", example = "1")
    Integer peopleId,
    
    @Schema(description = "Nome da pessoa", example = "João Silva")
    String name
) {}
