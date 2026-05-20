package br.com.sebratel.bff.dho.controller;

import java.util.List;
import java.time.LocalDateTime;

import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import br.com.sebratel.bff.dho.service.OpportunityService;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoPermissionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OpportunityController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OpportunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpportunityService opportunityService;

    @MockitoBean
    private PeopleRepository peopleRepository;

    @MockitoBean
    private DhoRoleRepository roleRepository;

    @MockitoBean
    private DhoPermissionRepository permissionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldApproveOpportunity() throws Exception {
        OpportunityApprovalDTO mockDto = new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), "A".repeat(200));
        when(opportunityService.approve(eq(1), any(OpportunityApprovalDTO.class))).thenReturn(new OpportunityResponseDTO());

        mockMvc.perform(post("/api/opportunities/1/approve").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRefuseOpportunityWithValidJustification() throws Exception {
        String justification = "A".repeat(200);
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), justification);
        
        when(opportunityService.refuse(eq(1), any(OpportunityApprovalDTO.class)))
                .thenReturn(new OpportunityResponseDTO());

        mockMvc.perform(post("/api/opportunities/1/refuse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
    @Test
    void shouldReproveOpportunityWithValidJustification() throws Exception {
        String justification = "A".repeat(200);
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), justification);
        
        when(opportunityService.refuse(eq(1), any(OpportunityApprovalDTO.class)))
                .thenReturn(new OpportunityResponseDTO());

        mockMvc.perform(post("/api/opportunities/1/reprove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenReprovingWithShortJustification() throws Exception {
        String justification = "Short justification";
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), justification);

        mockMvc.perform(post("/api/opportunities/1/reprove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldReturnBadRequestWhenRefusingWithShortJustification() throws Exception {
        String justification = "Short justification";
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), justification);

        mockMvc.perform(post("/api/opportunities/1/refuse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void shouldFinalizeOpportunityWithValidJustification() throws Exception {
        String justification = "A".repeat(200);
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), justification);

        when(opportunityService.finalize(eq(1), any(OpportunityApprovalDTO.class)))
                .thenReturn(new OpportunityResponseDTO());

        mockMvc.perform(post("/api/opportunities/1/finalize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenFinalizingWithShortJustification() throws Exception {
        String justification = "Short justification";
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), justification);

        mockMvc.perform(post("/api/opportunities/1/finalize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListCandidatesByOpportunity() throws Exception {
        when(opportunityService.findCandidatesByOpportunityId(1)).thenReturn(List.of());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/opportunities/1/pipeline"))
                .andExpect(status().isOk());
    }

}
