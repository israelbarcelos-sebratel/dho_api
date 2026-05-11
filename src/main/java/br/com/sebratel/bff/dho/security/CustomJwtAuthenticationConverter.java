package br.com.sebratel.bff.dho.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRole;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoPermission;
import br.com.sebratel.bff.dho.domain.repository.DhoPermissionRepository;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final PeopleRepository peopleRepository;
    private final DhoRoleRepository roleRepository;
    private final DhoPermissionRepository permissionRepository;

    private static final Logger log = LoggerFactory.getLogger(CustomJwtAuthenticationConverter.class);

    @Override
    @Transactional
    public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            String email = jwt.getClaimAsString("email");
            String name = jwt.getClaimAsString("name");
            log.info("Tentando autenticar usuário: {} ({})", email, name);

            People person = peopleRepository.findByEmail(email)
                    .orElseGet(() -> {
                        log.info("Usuário não encontrado no banco, auto-provisionando: {}", email);
                        return autoProvisionUser(email, name);
                    });

            Collection<GrantedAuthority> authorities = extractAuthorities(person);
            log.info("Usuário {} autenticado com sucesso. Autoridades: {}", 
                email, 
                authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
            
            return new JwtAuthenticationToken(jwt, authorities, email);
        } catch (Exception e) {
            log.error("FALHA NA CONVERSÃO DO JWT PARA O USUÁRIO: " + jwt.getClaimAsString("email"), e);
            throw e;
        }
    }

    private People autoProvisionUser(String email, String name) {
        DhoRole defaultRole = roleRepository.findByName("COLABORADOR")
                .orElseGet(() -> {
                    DhoPermission defaultPermission = permissionRepository.findByName("DEFAULT")
                            .orElseGet(() -> permissionRepository.save(DhoPermission.builder()
                                    .name("DEFAULT")
                                    .description("Permissão padrão")
                                    .build()));

                    return roleRepository.save(DhoRole.builder()
                            .name("COLABORADOR")
                            .description("Papel padrão para novos usuários")
                            .permissions(new HashSet<>(Collections.singletonList(defaultPermission)))
                            .build());
                });

        People newPerson = People.builder()
                .email(email)
                .name(name)
                .roles(new HashSet<>(Collections.singletonList(defaultRole)))
                .build();

        return peopleRepository.save(newPerson);
    }

    private Collection<GrantedAuthority> extractAuthorities(People person) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        if (person.getRoles() != null) {
            for (DhoRole role : person.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));
                
                if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
                    List<SimpleGrantedAuthority> permissions = role.getPermissions().stream()
                            .map(DhoPermission::getName)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    authorities.addAll(permissions);
                }
            }
        }
        
        return authorities;
    }
}
