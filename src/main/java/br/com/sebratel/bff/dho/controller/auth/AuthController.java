package br.com.sebratel.bff.dho.controller.auth;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRole;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.dto.UserResponseDTO;
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
public class AuthController {

    private final PeopleRepository peopleRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        People person = peopleRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        List<String> permissions = Collections.emptyList();
        String roles = null;
        if (person.getRoles() != null && !person.getRoles().isEmpty()) {
            permissions = person.getRoles().stream()
                    .filter(role -> role.getPermissions() != null)
                    .flatMap(role -> role.getPermissions().stream())
                    .map(DhoPermission::getName)
                    .distinct()
                    .collect(Collectors.toList());
            
            roles = person.getRoles().stream()
                    .map(DhoRole::getName)
                    .collect(Collectors.joining(", "));
        }

        return ResponseEntity.ok(UserResponseDTO.builder()
                .id(person.getId())
                .name(person.getName())
                .email(person.getEmail())
                .role(roles)
                .permissions(permissions)
                .build());
    }
}
