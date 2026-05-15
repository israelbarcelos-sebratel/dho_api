package br.com.sebratel.bff.dho.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Dados para criação ou atualização de uma oportunidade/vaga")
public record OpportunityRequestDTO(
    @Schema(description = "Data de abertura da vaga", example = "2024-05-20T10:00:00")
    LocalDateTime openOpportunityDate,
    
    @Schema(description = "ID do candidato vinculado (opcional)", example = "123")
    Integer candidateId,
    
    @Schema(description = "ID do cargo", example = "1")
    @NotNull(message = "O cargo é obrigatório")
    Integer positionId,
    
    @Schema(description = "ID do time", example = "5")
    @NotNull(message = "O time é obrigatório")
    Integer teamId,
    
    @Schema(description = "ID do departamento", example = "2")
    @NotNull(message = "O departamento é obrigatório")
    Integer departmentId,
    
    @Schema(description = "ID do motivo da abertura", example = "1")
    @NotNull(message = "O motivo da abertura é obrigatório")
    Integer opportunityMotiveId,

    @Schema(description = "Horário ou modelo de trabalho", example = "Segunda a Sexta, 08:00 às 18:00")
    @NotBlank(message = "O horário/modelo é obrigatório")
    String workSchedule,

    @Schema(description = "Habilidades técnicas necessárias", example = "Java, Spring Boot, SQL")
    @NotBlank(message = "A definição de hard skills é obrigatória")
    String hardSkills,

    @Schema(description = "Habilidades comportamentais necessárias", example = "Comunicação, Proatividade")
    @NotBlank(message = "A definição de soft skills é obrigatória")
    String softSkills,

    @Schema(description = "ID da pessoa substituída (se houver)", example = "45")
    Integer replacedPersonId,
    
    @Schema(description = "ID da base de origem", example = "1")
    Integer baseOriginId,
    
    @Schema(description = "ID do status inicial", example = "1")
    Integer opportunityStatusId,
    
    @Schema(description = "ID da etapa inicial do processo", example = "1")
    Integer processStageId,
    
    @Schema(description = "ID do status inicial do processo", example = "1")
    Integer processStatusId,
    
    @Schema(description = "Prazo SLA em dias", example = "15")
    Integer deadlineSlaDays,
    
    @Schema(description = "Data de aceite da vaga", example = "2024-05-21T09:00:00")
    LocalDateTime acceptDate,
    
    @Schema(description = "ID do recrutador responsável", example = "10")
    Integer responsibleRecruiterId,
    
    @Schema(description = "Observações adicionais", example = "Vaga urgente para projeto X")
    String observations
) {
}
