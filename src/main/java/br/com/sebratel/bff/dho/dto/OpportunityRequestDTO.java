package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record OpportunityRequestDTO(
    LocalDateTime openOpportunityDate,
    
    Integer candidateId,
    
    @NotNull(message = "O cargo é obrigatório")
    Integer positionId,
    
    @NotNull(message = "O time é obrigatório")
    Integer teamId,
    
    @NotNull(message = "O departamento é obrigatório")
    Integer departmentId,
    
    @NotNull(message = "O motivo da abertura é obrigatório")
    Integer opportunityMotiveId,

    @NotBlank(message = "O horário/modelo é obrigatório")
    String workSchedule,

    @NotBlank(message = "A definição de hard skills é obrigatória")
    String hardSkills,

    @NotBlank(message = "A definição de soft skills é obrigatória")
    String softSkills,

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
