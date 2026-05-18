package br.com.sebratel.bff.dho.controller.auth;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRole;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRole;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoPermission;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para consulta de informações do usuário autenticado")
public class AuthController {

    private final PeopleRepository peopleRepository;

    @GetMapping("/me")
    @Operation(summary = "Obter dados do usuário atual", description = "Retorna os detalhes, cargos e permissões do usuário autenticado via JWT.")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        People person = peopleRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        List<String> permissions = Collections.emptyList();
        List<String> rolesList = Collections.emptyList();
        if (person.getRoles() != null && !person.getRoles().isEmpty()) {
            permissions = person.getRoles().stream()
                    .filter(role -> role.getPermissions() != null)
                    .flatMap(role -> role.getPermissions().stream())
                    .map(DhoPermission::getName)
                    .map(Enum::name)
                    .distinct()
                    .collect(Collectors.toList());
            
            rolesList = person.getRoles().stream()
                    .map(DhoRole::getName)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(UserResponseDTO.builder()
                .id(person.getId())
                .name(person.getName())
                .email(person.getEmail())
                .roles(rolesList)
                .permissions(permissions)
                .build());
    }
}
