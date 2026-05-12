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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;

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

        mockMvc.perform(post("/requisitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(jwt().jwt(builder -> builder
                        .claim("email", "manager@test.com")
                        .subject("manager@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Developer"))
                .andExpect(jsonPath("$[0].status").value("Enviado para aprovação"));
    }

    @Test
    @Transactional
    void shouldReturnAllRequisitionsForAdminWhenRequested() throws Exception {
        People requester1 = peopleRepository.save(People.builder().name("User 1").email("user1@test.com").build());
        People admin = peopleRepository.save(People.builder().name("Admin").email("admin@test.com").build());

        DhoPosition position = DhoPosition.builder().name("Dev").build();
        entityManager.persist(position);
        DhoTeam team = DhoTeam.builder().name("IT").build();
        entityManager.persist(team);
        DhoDepartment dept = DhoDepartment.builder().name("IT").build();
        entityManager.persist(dept);
        DhoOpportunityMotive motive = DhoOpportunityMotive.builder().name("New").build();
        entityManager.persist(motive);
        DhoOpportunityStatus status = DhoOpportunityStatus.builder().name("Pendente").build();
        entityManager.persist(status);

        opportunityRepository.save(Opportunity.builder()
                .position(position).team(team).department(dept).opportunityMotive(motive)
                .opportunityStatus(status).openOpportunityDate(LocalDateTime.now())
                .requester(requester1).build());

        opportunityRepository.save(Opportunity.builder()
                .position(position).team(team).department(dept).opportunityMotive(motive)
                .opportunityStatus(status).openOpportunityDate(LocalDateTime.now())
                .requester(admin).build());

        // Admin requesting ONLY THEIR OWN (default)
        mockMvc.perform(post("/requisitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(jwt().jwt(builder -> builder
                        .claim("email", "admin@test.com")
                        .subject("admin@test.com"))
                        .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Admin requesting ALL
        mockMvc.perform(post("/requisitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"showAllRequisitions\": true}")
                .with(jwt().jwt(builder -> builder
                        .claim("email", "admin@test.com")
                        .subject("admin@test.com"))
                        .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Transactional
    void shouldNotReturnAllRequisitionsForRegularUserEvenIfRequested() throws Exception {
        People requester1 = peopleRepository.save(People.builder().name("User 1").email("user1@test.com").build());
        People user2 = peopleRepository.save(People.builder().name("User 2").email("user2@test.com").build());

        DhoPosition position = DhoPosition.builder().name("Dev").build();
        entityManager.persist(position);
        DhoTeam team = DhoTeam.builder().name("IT").build();
        entityManager.persist(team);
        DhoDepartment dept = DhoDepartment.builder().name("IT").build();
        entityManager.persist(dept);
        DhoOpportunityMotive motive = DhoOpportunityMotive.builder().name("New").build();
        entityManager.persist(motive);
        DhoOpportunityStatus status = DhoOpportunityStatus.builder().name("Pendente").build();
        entityManager.persist(status);

        opportunityRepository.save(Opportunity.builder()
                .position(position).team(team).department(dept).opportunityMotive(motive)
                .opportunityStatus(status).openOpportunityDate(LocalDateTime.now())
                .requester(requester1).build());

        // User 2 requesting ALL (should fail and return only their own, which is 0)
        mockMvc.perform(post("/requisitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"showAllRequisitions\": true}")
                .with(jwt().jwt(builder -> builder
                        .claim("email", "user2@test.com")
                        .subject("user2@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
