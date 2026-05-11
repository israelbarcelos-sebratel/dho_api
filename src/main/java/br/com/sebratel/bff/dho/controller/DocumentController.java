package br.com.sebratel.bff.dho.controller;
import org.springframework.security.access.prepost.PreAuthorize;


import br.com.sebratel.bff.dho.dto.DocumentResponseDTO;
import br.com.sebratel.bff.dho.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Documentos", description = "Endpoints para upload, download e gestão de documentos")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).ADD_CANDIDATE.name())")
    @Operation(summary = "Upload de documentos", description = "Permite o envio de múltiplos arquivos associados a um candidato e tipo de documento.")
    public ResponseEntity<List<DocumentResponseDTO>> uploadDocuments(
            @RequestParam("candidateId") Integer candidateId,
            @RequestParam("documentTypeId") Integer documentTypeId,
            @Parameter(description = "Arquivos a serem enviados", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("files") MultipartFile[] files) throws IOException {
        
        List<DocumentResponseDTO> response = documentService.uploadDocuments(candidateId, documentTypeId, files);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).VIEW_JOB_TRACKING.name())")
    @Operation(summary = "Listar documentos por candidato", description = "Retorna a lista de metadados dos documentos de um candidato específico.")
    public ResponseEntity<List<DocumentResponseDTO>> getDocumentsByCandidate(@PathVariable Integer candidateId) {
        List<DocumentResponseDTO> response = documentService.getDocumentsByCandidate(candidateId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).VIEW_JOB_TRACKING.name())")
    @Operation(summary = "Download de documento", description = "Faz o download do arquivo físico do documento pelo seu ID.")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Integer id) throws IOException {
        return documentService.downloadDocument(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).VIEW_JOB_TRACKING.name())")
    @Operation(summary = "Excluir documento", description = "Remove o registro do documento e o arquivo físico associado.")
    public ResponseEntity<Void> deleteDocument(@PathVariable Integer id) throws IOException {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
