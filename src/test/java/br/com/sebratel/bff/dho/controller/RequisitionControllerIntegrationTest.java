package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.*;
import br.com.sebratel.bff.dho.domain.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RequisitionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void shouldReturnRequisitionsForManager() throws Exception {
        People requester = People.builder()
                .name("Manager User")
                .email("manager@test.com")
                .build();
        requester = peopleRepository.save(requester);

        DhoPosition position = DhoPosition.builder().name("Developer").build();
        entityManager.persist(position);
        
        DhoTeam team = DhoTeam.builder().name("IT").build();
        entityManager.persist(team);
        
        DhoDepartment dept = DhoDepartment.builder().name("Engineering").build();
        entityManager.persist(dept);
        
        DhoOpportunityMotive motive = DhoOpportunityMotive.builder().name("New Position").build();
        entityManager.persist(motive);
        
        DhoOpportunityStatus status = DhoOpportunityStatus.builder().name("Pendente").build();
        entityManager.persist(status);

        Opportunity opp = Opportunity.builder()
                .position(position)
                .team(team)
                .department(dept)
                .opportunityMotive(motive)
                .opportunityStatus(status)
                .openOpportunityDate(LocalDateTime.now())
                .workSchedule("Remoto")
                .requester(requester)
                .build();
        opportunityRepository.save(opp);

        mockMvc.perform(get("/requisitions")
                .with(jwt().jwt(builder -> builder
                        .claim("email", "manager@test.com")
                        .subject("manager@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Developer"))
                .andExpect(jsonPath("$[0].status").value("Enviado para aprovação"));
    }
}
