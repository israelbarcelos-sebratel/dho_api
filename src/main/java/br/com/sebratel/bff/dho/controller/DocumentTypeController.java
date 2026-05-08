package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoDocumentType;
import br.com.sebratel.bff.dho.domain.repository.DhoDocumentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/document-types")
@RequiredArgsConstructor
public class DocumentTypeController {

    private final DhoDocumentTypeRepository documentTypeRepository;

    @GetMapping
    public ResponseEntity<List<DhoDocumentType>> getAllDocumentTypes() {
        return ResponseEntity.ok(documentTypeRepository.findAll());
    }
}
