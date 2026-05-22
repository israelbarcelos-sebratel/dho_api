package br.com.sebratel.bff.dho.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDetailDTO {
    private Integer id;
    private Integer talentPoolId;
    private String name;
    private String email;
}
