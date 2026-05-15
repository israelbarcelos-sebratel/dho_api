package br.com.sebratel.bff.dho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDecisionDTO {
    private boolean accepted;
    @NotBlank(message = "O motivo é obrigatório")
    @Size(min = 200, message = "deve ter no mínimo 200 caracteres")
    private String reason;
}
