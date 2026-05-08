package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record OpportunityRequestDTO(
    @NotNull(message = "A data de abertura é obrigatória")
    LocalDateTime openOpportunityDate,
    
    Integer candidateId,
    
    @NotNull(message = "O cargo é obrigatório")
    Integer positionId,
    
    Integer teamId,
    
    @NotNull(message = "O departamento é obrigatório")
    Integer departmentId,
    
    Integer opportunityMotiveId,
    Integer replacedPersonId,
    Integer baseOriginId,
    Integer opportunityStatusId,
    Integer processStageId,
    Integer processStatusId,
    Integer deadlineSlaDays,
    LocalDateTime acceptDate,
    Integer responsibleRecruiterId,
    String observations
) {
}
