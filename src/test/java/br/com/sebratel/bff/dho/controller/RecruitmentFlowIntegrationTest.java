package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.*;
import br.com.sebratel.bff.dho.domain.repository.*;
import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RecruitmentFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;
    @Autowired private PeopleRepository peopleRepository;
    @Autowired private OpportunityRepository opportunityRepository;
    @Autowired private RecruitmentProcessRepository recruitmentProcessRepository;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        String[] statuses = {"Pendente", "Aprovada", "Em andamento", "Aprovado", "Reprovado", "Enviada Proposta", "Finalizado", "Recusada pelo candidato", "Recusada"};
        for (String s : statuses) createStatusIfNotExist(s);
        String[] stages = {"Banco de Talentos", "Triagem", "Entrevista", "Teste Técnico", "Decisão Final", "Aprovado"};
        for (String s : stages) createStageIfNotExist(s);
    }

    private void createStatusIfNotExist(String name) {
        if (entityManager.createQuery("SELECT s FROM DhoProcessStatus s WHERE s.name = :n").setParameter("n", name).getResultList().isEmpty())
            entityManager.persist(DhoProcessStatus.builder().name(name).build());
        if (entityManager.createQuery("SELECT s FROM DhoOpportunityStatus s WHERE s.name = :n").setParameter("n", name).getResultList().isEmpty())
            entityManager.persist(DhoOpportunityStatus.builder().name(name).build());
    }

    private void createStageIfNotExist(String name) {
        if (entityManager.createQuery("SELECT s FROM DhoProcessStage s WHERE s.name = :n").setParameter("n", name).getResultList().isEmpty())
            entityManager.persist(DhoProcessStage.builder().name(name).build());
    }

    @Test
    void testCompleteRecruitmentFlow() throws Exception {
        Integer oppId = createOpportunity(); Integer candId = createCandidate();
        MvcResult res = mockMvc.perform(post("/api/recruitment-processes").contentType(MediaType.APPLICATION_JSON).content(String.format("{\"candidateId\": %d, \"opportunityId\": %d}", candId, oppId)).with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("initiate_contract_process")))).andExpect(status().isOk()).andReturn();
        Integer id = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asInt();
        
        mockMvc.perform(post("/api/recruitment-processes/"+id+"/move-to-screening").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("approve_candidate")))).andExpect(status().isOk());

        mockMvc.perform(post("/api/recruitment-processes/"+id+"/move-to-interview").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("approve_candidate")))).andExpect(status().isOk());
        mockMvc.perform(post("/api/recruitment-processes/"+id+"/move-to-technical-test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Parecer técnico com mais de dez caracteres\"}")
                .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("approve_candidate")))).andExpect(status().isOk());
        mockMvc.perform(post("/api/recruitment-processes/"+id+"/move-to-final-decision").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("approve_candidate")))).andExpect(status().isOk());
        mockMvc.perform(post("/api/recruitment-processes/"+id+"/manager-decision").contentType(MediaType.APPLICATION_JSON).content("{\"approved\": true, \"reason\": \"" + "A".repeat(200) + "\"}").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("reject_candidate")))).andExpect(status().isOk());
        mockMvc.perform(post("/api/recruitment-processes/"+id+"/proposal").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("initiate_contract_process")))).andExpect(status().isOk());
        mockMvc.perform(post("/api/recruitment-processes/"+id+"/candidate-decision").contentType(MediaType.APPLICATION_JSON).content("{\"accepted\": true, \"reason\": \"" + "A".repeat(200) + "\"}").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("initiate_contract_process")))).andExpect(status().isOk());
        validateStatus(id, "Finalizado");
    }
    @Test
    void testReproveOpportunityFlow() throws Exception {
        Integer oppId = createPendingOpportunity();
        
        String justification = "A".repeat(200);
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), justification);

        mockMvc.perform(post("/api/opportunities/" + oppId + "/reprove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("approve_contract_process"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opportunityStatusName").value("Recusada"))
                .andExpect(jsonPath("$.refusalJustification").value(justification));
    }

    private Integer createPendingOpportunity() {
        DhoPosition pos = DhoPosition.builder().name("Dev Pending").build(); 
        entityManager.persist(pos);
        DhoOpportunityStatus status = (DhoOpportunityStatus) entityManager.createQuery("SELECT s FROM DhoOpportunityStatus s WHERE s.name = 'Pendente'").getSingleResult();
        Opportunity opp = Opportunity.builder()
                .position(pos)
                .opportunityStatus(status)
                .openOpportunityDate(LocalDateTime.now().plusDays(1))
                .build();
        return opportunityRepository.save(opp).getId();
    }


    @Test
    void testSecurityResistances() throws Exception {
        Integer oppId = createOpportunity(); Integer candId = createCandidate();
        MvcResult res = mockMvc.perform(post("/api/recruitment-processes").contentType(MediaType.APPLICATION_JSON).content(String.format("{\"candidateId\": %d, \"opportunityId\": %d}", candId, oppId)).with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("initiate_contract_process")))).andReturn();
        Integer id = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asInt();

        // 1. Tente fazer alguém sem role de recrutadora (sem approve_candidate) transicionar um candidato de triagem para entrevista
        mockMvc.perform(post("/api/recruitment-processes/"+id+"/move-to-interview").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("suggestions")))).andExpect(status().isForbidden());

        // 2. Tente fazer com que alguém sem role de gestor (sem reject_candidate) tente aprovar um candidato
        mockMvc.perform(post("/api/recruitment-processes/"+id+"/manager-decision").contentType(MediaType.APPLICATION_JSON).content("{\"approved\": true, \"reason\": \"" + "A".repeat(200) + "\"}").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("approve_candidate")))).andExpect(status().isForbidden());

        // 3. Tente fazer com que alguém sem role de recrutadora (sem initiate_contract_process) finalize o processo
        mockMvc.perform(post("/api/recruitment-processes/"+id+"/candidate-decision").contentType(MediaType.APPLICATION_JSON).content("{\"accepted\": true, \"reason\": \"" + "A".repeat(200) + "\"}").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("reject_candidate")))).andExpect(status().isForbidden());
    }

    @Test
    void testBusinessLogicResistances() throws Exception {
        Integer oppId = createOpportunity(); Integer candId = createCandidate();
        MvcResult res = mockMvc.perform(post("/api/recruitment-processes").contentType(MediaType.APPLICATION_JSON).content(String.format("{\"candidateId\": %d, \"opportunityId\": %d}", candId, oppId)).with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("initiate_contract_process")))).andReturn();
        Integer id = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asInt();

        // Tente pular estágio: Manager decision direto da Triagem
        mockMvc.perform(post("/api/recruitment-processes/"+id+"/manager-decision").contentType(MediaType.APPLICATION_JSON).content("{\"approved\": true}").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("reject_candidate")))).andExpect(status().isBadRequest());

        // Tente pular estágio: Proposal direto da Triagem
        mockMvc.perform(post("/api/recruitment-processes/"+id+"/proposal").with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("initiate_contract_process")))).andExpect(status().isBadRequest());
    }

    private Integer createOpportunity() {
        DhoPosition pos = DhoPosition.builder().name("Dev").build(); entityManager.persist(pos);
        DhoOpportunityStatus status = (DhoOpportunityStatus) entityManager.createQuery("SELECT s FROM DhoOpportunityStatus s WHERE s.name = 'Aprovada'").getSingleResult();
        Opportunity opp = Opportunity.builder().position(pos).opportunityStatus(status).openOpportunityDate(LocalDateTime.now().plusDays(1)).build();
        return opportunityRepository.save(opp).getId();
    }
    @Test
    void testAssignRecruiterFlow() throws Exception {
        Integer oppId = createOpportunity();
        Integer recruiterId = createCandidate(); // Use candidate helper as it just creates a person

        // Testa sem permissão
        mockMvc.perform(post("/api/opportunities/" + oppId + "/assign-recruiter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"recruiterId\": %d}", recruiterId))
                .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("suggestions"))))
                .andExpect(status().isForbidden());

        // Testa com permissão
        mockMvc.perform(post("/api/opportunities/" + oppId + "/assign-recruiter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"recruiterId\": %d}", recruiterId))
                .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("assign_recruiter"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsibleRecruiterName").value("Cand"));

        // Valida persistência
        Opportunity opp = opportunityRepository.findById(oppId).orElseThrow();
        assertEquals(recruiterId, opp.getResponsibleRecruiter().getId());
    }


    private Integer createCandidate() { return peopleRepository.save(People.builder().name("Cand").email("c@t.com").build()).getId(); }
    private void validateStatus(Integer id, String e) { assertEquals(e, recruitmentProcessRepository.findById(id).orElseThrow().getProcessStatus().getName()); }
}
