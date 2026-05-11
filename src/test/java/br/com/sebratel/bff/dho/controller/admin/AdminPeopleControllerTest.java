package br.com.sebratel.bff.dho.controller.admin;

import br.com.sebratel.bff.dho.dto.UserRolesResponseDTO;
import br.com.sebratel.bff.dho.service.AdminPeopleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import br.com.sebratel.bff.dho.security.CustomJwtAuthenticationConverter;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoPermissionRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminPeopleController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class AdminPeopleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminPeopleService adminPeopleService;
    @MockitoBean
    private CustomJwtAuthenticationConverter customJwtAuthenticationConverter;
    @MockitoBean
    private PeopleRepository peopleRepository;
    @MockitoBean
    private DhoRoleRepository dhoRoleRepository;
    @MockitoBean
    private DhoPermissionRepository dhoPermissionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_ShouldReturnList() throws Exception {
        UserRolesResponseDTO response = UserRolesResponseDTO.builder()
                .id(1)
                .name("User")
                .email("user@test.com")
                .build();
        when(adminPeopleService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("user@test.com"));
    }

    @Test
    void assignRoles_ShouldReturnOk() throws Exception {
        Set<Integer> roleIds = Set.of(1, 2);
        UserRolesResponseDTO response = UserRolesResponseDTO.builder().id(1).build();
        when(adminPeopleService.assignRoles(eq(1), any(Set.class))).thenReturn(response);

        mockMvc.perform(put("/api/admin/people/1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isOk());
    }
}
