package br.com.sebratel.bff.dho.controller.admin;

import br.com.sebratel.bff.dho.dto.PermissionResponseDTO;
import br.com.sebratel.bff.dho.dto.RoleRequestDTO;
import br.com.sebratel.bff.dho.dto.RoleResponseDTO;
import br.com.sebratel.bff.dho.service.DhoRoleService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DhoRoleController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class DhoRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DhoRoleService roleService;
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
        RoleResponseDTO response = RoleResponseDTO.builder()
                .id(1)
                .name("ADMIN")
                .description("Administrator")
                .permissions(Set.of(new PermissionResponseDTO(1, "CAN_VIEW", "Can view")))
                .build();
        when(roleService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    void create_WithValidData_ShouldReturnCreated() throws Exception {
        RoleRequestDTO request = new RoleRequestDTO("ADMIN", "Administrator");
        RoleResponseDTO response = RoleResponseDTO.builder().id(1).name("ADMIN").description("Administrator").build();
        when(roleService.create(any(RoleRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    void assignPermissions_ShouldReturnOk() throws Exception {
        Set<Integer> permissionIds = Set.of(1, 2);
        RoleResponseDTO response = RoleResponseDTO.builder().id(1).name("ADMIN").build();
        when(roleService.assignPermissions(eq(1), any(Set.class))).thenReturn(response);

        mockMvc.perform(put("/api/admin/roles/1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andExpect(status().isOk());
    }
}
