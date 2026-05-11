package br.com.sebratel.bff.dho.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestResponseDTO {
    private Integer id;
    private Integer personId;
    private String personName;
    private String personEmail;
    private Integer roleId;
    private String roleName;
    private String status;
    private LocalDateTime requestDate;
    private LocalDateTime resolutionDate;
}
