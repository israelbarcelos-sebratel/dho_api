package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.DocumentResponseDTO;
import br.com.sebratel.bff.dho.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DocumentResponseDTO>> uploadDocuments(
            @RequestParam("candidateId") Integer candidateId,
            @RequestParam("documentTypeId") Integer documentTypeId,
            @RequestParam("files") MultipartFile[] files) throws IOException {
        
        List<DocumentResponseDTO> response = documentService.uploadDocuments(candidateId, documentTypeId, files);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<DocumentResponseDTO>> getDocumentsByCandidate(@PathVariable Integer candidateId) {
        List<DocumentResponseDTO> response = documentService.getDocumentsByCandidate(candidateId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Integer id) throws IOException {
        return documentService.downloadDocument(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Integer id) throws IOException {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
