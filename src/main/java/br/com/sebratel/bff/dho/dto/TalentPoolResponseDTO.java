package br.com.sebratel.bff.dho.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TalentPoolResponseDTO {
    private Integer id;
    private Integer peopleId;
    private String name;
    private String email;
    private String phoneNumber;
    private String observations;
    private Set<String> suggestedPositions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
