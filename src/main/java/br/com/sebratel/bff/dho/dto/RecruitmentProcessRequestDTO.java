package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentProcessRequestDTO {
    @NotNull(message = "O ID do candidato é obrigatório")
    private Integer candidateId;
    
    @NotNull(message = "O ID da oportunidade é obrigatório")
    private Integer opportunityId;
}
