package br.com.sebratel.bff.dho.controller.admin;

import br.com.sebratel.bff.dho.dto.PermissionRequestDTO;
import br.com.sebratel.bff.dho.dto.PermissionResponseDTO;
import br.com.sebratel.bff.dho.service.DhoPermissionService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DhoPermissionController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class DhoPermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DhoPermissionService permissionService;
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
        PermissionResponseDTO response = new PermissionResponseDTO(1, "CAN_VIEW", "Can view documents");
        when(permissionService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("CAN_VIEW"));
    }

    @Test
    void create_WithValidData_ShouldReturnCreated() throws Exception {
        PermissionRequestDTO request = new PermissionRequestDTO("CAN_VIEW", "Can view documents");
        PermissionResponseDTO response = new PermissionResponseDTO(1, "CAN_VIEW", "Can view documents");
        when(permissionService.create(any(PermissionRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("CAN_VIEW"));
    }
}
