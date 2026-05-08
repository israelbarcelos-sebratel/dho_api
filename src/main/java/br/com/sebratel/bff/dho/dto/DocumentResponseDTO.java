package br.com.sebratel.bff.dho.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponseDTO {
    private Integer id;
    private String fileName;
    private String fileType;
    private String documentTypeName;
    private LocalDateTime uploadDate;
}
