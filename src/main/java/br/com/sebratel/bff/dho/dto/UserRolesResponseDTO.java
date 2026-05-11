package br.com.sebratel.bff.dho.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRolesResponseDTO {
    private Integer id;
    private String name;
    private String email;
    private Set<RoleResponseDTO> roles;
}
