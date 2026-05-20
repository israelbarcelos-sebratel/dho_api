package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoOpportunityMotive;
import br.com.sebratel.bff.dho.domain.repository.DhoOpportunityMotiveRepository;
import br.com.sebratel.bff.dho.security.CustomJwtAuthenticationConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OpportunityMotiveController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OpportunityMotiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DhoOpportunityMotiveRepository opportunityMotiveRepository;

    @MockitoBean
    private CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    @MockitoBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Test
    void getAllOpportunityMotives_ShouldReturnList() throws Exception {
        DhoOpportunityMotive motive = DhoOpportunityMotive.builder()
                .id(1)
                .name("Aumento de quadro")
                .description("Abertura por aumento de quadro")
                .build();

        when(opportunityMotiveRepository.findAll()).thenReturn(List.of(motive));

        mockMvc.perform(get("/api/opportunity-motives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Aumento de quadro"))
                .andExpect(jsonPath("$[0].description").value("Abertura por aumento de quadro"));
    }
}
