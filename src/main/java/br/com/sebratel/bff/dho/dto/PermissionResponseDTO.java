package br.com.sebratel.bff.dho.dto;

import br.com.sebratel.bff.dho.domain.enums.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDTO {
    private Integer id;
    private Permission name;
    private String description;
}
