package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.repository.DhoPermissionRepository;
import java.time.LocalDateTime;
import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.dto.TechnicalTestRequestDTO;
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
import org.springframework.security.test.context.support.WithMockUser;
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

@WebMvcTest(controllers = RecruitmentProcessController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RecruitmentProcessControllerTest {

    private static final String BASE_URL = "/api/recruitment-processes";

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

    @MockitoBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void approve_ShouldReturnOk() throws Exception {
        doNothing().when(recruitmentProcessService).approve(eq(1), any(OpportunityApprovalDTO.class));

        mockMvc.perform(post(BASE_URL + "/1/approve").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), "Justification of at least 200 characters".repeat(10)))))
                .andExpect(status().isOk());
    }

    @Test
    void refuse_ShouldReturnOk_WhenReportIsValid() throws Exception {
        String longReport = "A".repeat(200);
        InterviewDecisionDTO dto = new InterviewDecisionDTO(longReport);
        doNothing().when(recruitmentProcessService).refuse(eq(1), any(InterviewDecisionDTO.class));

        mockMvc.perform(post(BASE_URL + "/1/refuse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void refuse_ShouldReturnBadRequest_WhenReportIsTooShort() throws Exception {
        InterviewDecisionDTO dto = new InterviewDecisionDTO("Too short");

        mockMvc.perform(post(BASE_URL + "/1/refuse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_ShouldReturnOk() throws Exception {
        doNothing().when(recruitmentProcessService).withdraw(1);

        mockMvc.perform(post(BASE_URL + "/1/withdraw"))
                .andExpect(status().isOk());
    }

    @Test
    void hire_ShouldReturnOk() throws Exception {
        doNothing().when(recruitmentProcessService).hire(1);

        mockMvc.perform(post(BASE_URL + "/1/hire"))
                .andExpect(status().isOk());
    }

    @Test
    void getHistory_ShouldReturnOk() throws Exception {
        mockMvc.perform(get(BASE_URL + "/history"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "recruiter@sebratel.com.br")
    void getMyProcesses_ShouldReturnOk() throws Exception {
        mockMvc.perform(get(BASE_URL + "/mine"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStage_ShouldReturnOk() throws Exception {
        RecruitmentProcessStageDTO dto = new RecruitmentProcessStageDTO("Entrevista");
        doNothing().when(recruitmentProcessService).updateStage(eq(1), any(RecruitmentProcessStageDTO.class));

        mockMvc.perform(patch(BASE_URL + "/1/stage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_ShouldReturnOk() throws Exception {
        RecruitmentProcessStatusDTO dto = new RecruitmentProcessStatusDTO("Teste técnico");
        doNothing().when(recruitmentProcessService).updateStatus(eq(1), any(RecruitmentProcessStatusDTO.class));

        mockMvc.perform(patch(BASE_URL + "/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void getIndicators_ShouldReturnOk() throws Exception {
        mockMvc.perform(get(BASE_URL + "/indicators"))
                .andExpect(status().isOk());
    }



    @Test
    void moveToScreening_ShouldReturnOk() throws Exception {
        doNothing().when(recruitmentProcessService).moveToScreening(1);

        mockMvc.perform(post(BASE_URL + "/1/move-to-screening"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    void moveToScreening_WithRecruiterRole_ShouldReturnOk() throws Exception {
        doNothing().when(recruitmentProcessService).moveToScreening(1);

        mockMvc.perform(post(BASE_URL + "/1/move-to-screening"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void moveToScreening_WithAdminRole_ShouldReturnOk() throws Exception {
        doNothing().when(recruitmentProcessService).moveToScreening(1);

        mockMvc.perform(post(BASE_URL + "/1/move-to-screening"))
                .andExpect(status().isOk());
    }


    @Test
    void moveToTechnicalTest_ShouldReturnOk_WhenReportIsValid() throws Exception {
        TechnicalTestRequestDTO dto = new TechnicalTestRequestDTO("Parecer técnico válido com mais de 10 caracteres");
        doNothing().when(recruitmentProcessService).moveToTechnicalTest(eq(1), any(TechnicalTestRequestDTO.class));

        mockMvc.perform(post(BASE_URL + "/1/move-to-technical-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void moveToTechnicalTest_ShouldReturnBadRequest_WhenReportIsTooShort() throws Exception {
        TechnicalTestRequestDTO dto = new TechnicalTestRequestDTO("Curto");

        mockMvc.perform(post(BASE_URL + "/1/move-to-technical-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

}
