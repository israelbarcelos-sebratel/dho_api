package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoDocumentType;
import br.com.sebratel.bff.dho.domain.repository.DhoDocumentTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/document-types")
@RequiredArgsConstructor
@Tag(name = "Tipos de Documento", description = "Endpoints para consulta de tipos de documentos aceitos")
public class DocumentTypeController {

    private final DhoDocumentTypeRepository documentTypeRepository;

    @GetMapping
    @Operation(summary = "Listar todos os tipos de documento", description = "Retorna os tipos de documentos cadastrados (Ex: RG, CPF, Comprovante de Residência).")
    public ResponseEntity<List<DhoDocumentType>> getAllDocumentTypes() {
        return ResponseEntity.ok(documentTypeRepository.findAll());
    }
}
