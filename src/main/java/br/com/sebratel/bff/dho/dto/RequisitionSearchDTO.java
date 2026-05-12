package br.com.sebratel.bff.dho.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequisitionSearchDTO {

    @Schema(description = "Se true, e o usuário tiver permissão, retorna todas as requisições. Se false ou sem permissão, retorna apenas as próprias.", defaultValue = "false")
    private Boolean showAllRequisitions = false;
}
