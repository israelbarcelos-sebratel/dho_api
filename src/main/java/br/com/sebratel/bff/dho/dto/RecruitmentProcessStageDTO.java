package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentProcessStageDTO {
    @NotBlank(message = "O nome do estágio é obrigatório")
    private String stageName;
}
