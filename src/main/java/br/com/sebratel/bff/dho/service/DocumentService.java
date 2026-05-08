package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.DhoDocument;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoDocumentType;
import br.com.sebratel.bff.dho.domain.repository.DhoDocumentRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoDocumentTypeRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.dto.DocumentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DhoDocumentRepository documentRepository;
    private final DhoDocumentTypeRepository documentTypeRepository;
    private final PeopleRepository peopleRepository;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    public List<DocumentResponseDTO> uploadDocuments(Integer candidateId, Integer documentTypeId, MultipartFile[] files) throws IOException {
        People candidate = peopleRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidato não encontrado"));

        DhoDocumentType documentType = documentTypeRepository.findById(documentTypeId)
                .orElseThrow(() -> new RuntimeException("Tipo de documento não encontrado"));

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile file : files) {
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            DhoDocument document = DhoDocument.builder()
                    .fileName(originalFileName)
                    .fileType(file.getContentType())
                    .filePath(targetLocation.toString())
                    .candidate(candidate)
                    .documentType(documentType)
                    .uploadDate(LocalDateTime.now())
                    .build();

            documentRepository.save(document);
        }

        return getDocumentsByCandidate(candidateId);
    }

    public List<DocumentResponseDTO> getDocumentsByCandidate(Integer candidateId) {
        return documentRepository.findByCandidateId(candidateId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ResponseEntity<Resource> downloadDocument(Integer id) throws IOException {
        DhoDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

        Path filePath = Paths.get(document.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("Arquivo não encontrado no servidor");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    public void deleteDocument(Integer id) throws IOException {
        DhoDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

        Path filePath = Paths.get(document.getFilePath());
        Files.deleteIfExists(filePath);

        documentRepository.delete(document);
    }

    private DocumentResponseDTO mapToDTO(DhoDocument document) {
        return DocumentResponseDTO.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .documentTypeName(document.getDocumentType().getName())
                .uploadDate(document.getUploadDate())
                .build();
    }
}
