package br.com.sebratel.bff.dho.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record UserResponseDTO(
    Integer id,
    String name,
    String email,
    String role,
    List<String> permissions
) {}
