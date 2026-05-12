package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.repository.DhoPermissionRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.dto.InterviewDecisionDTO;
import br.com.sebratel.bff.dho.security.CustomJwtAuthenticationConverter;
import br.com.sebratel.bff.dho.service.RecruitmentProcessService;
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
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessStageDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessStatusDTO;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecruitmentProcessController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
public class RecruitmentProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecruitmentProcessService recruitmentProcessService;

    @MockitoBean
    private CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    @MockitoBean
    private PeopleRepository peopleRepository;

    @MockitoBean
    private DhoRoleRepository roleRepository;

    @MockitoBean
    private DhoPermissionRepository permissionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void approve_ShouldReturnOk() throws Exception {
        doNothing().when(recruitmentProcessService).approve(1);

        mockMvc.perform(post("/recruitment-processes/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    void refuse_ShouldReturnOk_WhenReportIsValid() throws Exception {
        String longReport = "A".repeat(200);
        InterviewDecisionDTO dto = new InterviewDecisionDTO(longReport);
        doNothing().when(recruitmentProcessService).refuse(eq(1), any(InterviewDecisionDTO.class));

        mockMvc.perform(post("/recruitment-processes/1/refuse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void refuse_ShouldReturnBadRequest_WhenReportIsTooShort() throws Exception {
        InterviewDecisionDTO dto = new InterviewDecisionDTO("Too short");

        mockMvc.perform(post("/recruitment-processes/1/refuse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_ShouldReturnOk() throws Exception {
        doNothing().when(recruitmentProcessService).withdraw(1);

        mockMvc.perform(post("/recruitment-processes/1/withdraw"))
                .andExpect(status().isOk());
    }

    @Test
    void hire_ShouldReturnOk() throws Exception {
        doNothing().when(recruitmentProcessService).hire(1);

        mockMvc.perform(post("/recruitment-processes/1/hire"))
                .andExpect(status().isOk());
    }

    @Test
    void getHistory_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/recruitment-processes/history"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyProcesses_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/recruitment-processes/mine")
                        .param("recruiterId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStage_ShouldReturnOk() throws Exception {
        RecruitmentProcessStageDTO dto = new RecruitmentProcessStageDTO("Entrevista");
        doNothing().when(recruitmentProcessService).updateStage(eq(1), any(RecruitmentProcessStageDTO.class));

        mockMvc.perform(patch("/recruitment-processes/1/stage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_ShouldReturnOk() throws Exception {
        RecruitmentProcessStatusDTO dto = new RecruitmentProcessStatusDTO("Teste técnico");
        doNothing().when(recruitmentProcessService).updateStatus(eq(1), any(RecruitmentProcessStatusDTO.class));

        mockMvc.perform(patch("/recruitment-processes/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void getIndicators_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/recruitment-processes/indicators"))
                .andExpect(status().isOk());
    }


}
