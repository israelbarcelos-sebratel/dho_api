package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.RoleRequestCreateDTO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DhoRoleRequestController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class DhoRoleRequestControllerTest {

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
    void createRequest_ShouldReturnCreated() throws Exception {
        RoleRequestCreateDTO request = new RoleRequestCreateDTO(1);
        RoleRequestResponseDTO response = RoleRequestResponseDTO.builder().id(1).status("PENDING").build();
        
        when(roleRequestService.createRequest(any(RoleRequestCreateDTO.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/roles/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
