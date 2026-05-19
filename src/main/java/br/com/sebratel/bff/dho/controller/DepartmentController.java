package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoDepartment;
import br.com.sebratel.bff.dho.domain.repository.DhoDepartmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
@Tag(name = "Departamentos", description = "Endpoints para consulta de departamentos")
public class DepartmentController {

    private final DhoDepartmentRepository departmentRepository;

    @GetMapping
    @Operation(summary = "Listar todos os departamentos", description = "Retorna uma lista com todos os departamentos disponíveis.")
    public ResponseEntity<List<DhoDepartment>> getAllDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }
}
