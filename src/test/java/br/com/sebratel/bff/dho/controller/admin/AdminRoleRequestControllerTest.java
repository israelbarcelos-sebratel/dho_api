package br.com.sebratel.bff.dho.controller.admin;

import br.com.sebratel.bff.dho.dto.RoleRequestResponseDTO;
import br.com.sebratel.bff.dho.service.DhoRoleRequestService;
import br.com.sebratel.bff.dho.security.CustomJwtAuthenticationConverter;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoPermissionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminRoleRequestController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class AdminRoleRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DhoRoleRequestService roleRequestService;

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
        RoleRequestResponseDTO response = RoleRequestResponseDTO.builder().id(1).status("PENDING").build();
        when(roleRequestService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/roles/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void approve_ShouldReturnOk() throws Exception {
        RoleRequestResponseDTO response = RoleRequestResponseDTO.builder().id(1).status("APPROVED").build();
        when(roleRequestService.approveRequest(eq(1))).thenReturn(response);

        mockMvc.perform(put("/api/admin/roles/requests/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void reject_ShouldReturnOk() throws Exception {
        RoleRequestResponseDTO response = RoleRequestResponseDTO.builder().id(1).status("REJECTED").build();
        when(roleRequestService.rejectRequest(eq(1))).thenReturn(response);

        mockMvc.perform(put("/api/admin/roles/requests/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }
}
