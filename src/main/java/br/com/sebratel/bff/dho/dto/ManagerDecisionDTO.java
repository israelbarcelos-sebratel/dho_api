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
public class ManagerDecisionDTO {
    private boolean approved;
    @NotBlank(message = "O motivo é obrigatório")
    @Size(min = 50, message = "deve ter no mínimo 50 caracteres")
    private String reason;
}
