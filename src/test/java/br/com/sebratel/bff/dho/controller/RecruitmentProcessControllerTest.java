package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.InterviewDecisionDTO;
import br.com.sebratel.bff.dho.service.RecruitmentProcessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecruitmentProcessController.class)
public class RecruitmentProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecruitmentProcessService recruitmentProcessService;

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

}
