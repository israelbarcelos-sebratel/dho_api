package br.com.sebratel.bff.dho.domain.entity;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoDocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DhoDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne
    @JoinColumn(name = "people_id")
    private People candidate;

    @ManyToOne
    @JoinColumn(name = "document_type_id")
    private DhoDocumentType documentType;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;
}
