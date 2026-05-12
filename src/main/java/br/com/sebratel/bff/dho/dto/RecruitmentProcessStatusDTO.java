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
public class RecruitmentProcessStatusDTO {
    @NotBlank(message = "O nome do status é obrigatório")
    private String statusName;
}
