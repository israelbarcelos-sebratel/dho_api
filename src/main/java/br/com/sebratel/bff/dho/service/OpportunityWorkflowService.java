package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoOpportunityStatus;
import br.com.sebratel.bff.dho.domain.repository.DhoOpportunityStatusRepository;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;
import br.com.sebratel.bff.dho.dto.OpportunityAssignRecruiterDTO;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import br.com.sebratel.bff.dho.mapper.OpportunityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityWorkflowService {

    private final OpportunityRepository opportunityRepository;
    private final DhoOpportunityStatusRepository statusRepository;
    private final PeopleRepository peopleRepository;
    private final OpportunityMapper opportunityMapper;

    @Transactional
    public OpportunityResponseDTO approve(Integer id, OpportunityApprovalDTO dto) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        if ("Aprovada".equals(opportunity.getOpportunityStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta oportunidade já está aprovada");
        }

        DhoOpportunityStatus approvedStatus = statusRepository.findByName("Aprovada")
                .orElseThrow(() -> new RuntimeException("Status 'Aprovada' não encontrado"));

        opportunity.setOpportunityStatus(approvedStatus);
        opportunity.setDeadlineSlaDays(30);
        opportunity.setOpenOpportunityDate(dto.opportunityDate());
        
        return opportunityMapper.convertToDTO(opportunityRepository.save(opportunity), false);
    }

    @Transactional
    public OpportunityResponseDTO refuse(Integer id, OpportunityApprovalDTO dto) {
        log.info("[OpportunityWorkflowService] refuse - Starting reproval process for opportunity ID: {}", id);
        
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[OpportunityWorkflowService] refuse - Opportunity not found with ID: {}", id);
                    return new RuntimeException("Oportunidade não encontrada");
                });

        DhoOpportunityStatus refusedStatus = statusRepository.findByName("Recusada")
                .orElseThrow(() -> {
                    log.error("[OpportunityWorkflowService] refuse - Status 'Recusada' not found in database");
                    return new RuntimeException("Status 'Recusada' não encontrado");
                });

        opportunity.setOpportunityStatus(refusedStatus);
        opportunity.setRefusalJustification(dto.reason());
        
        Opportunity savedOpportunity = opportunityRepository.save(opportunity);
        log.info("[OpportunityWorkflowService] refuse - Opportunity ID: {} successfully reproved", id);
        return opportunityMapper.convertToDTO(savedOpportunity, false);
    }

    @Transactional
    public OpportunityResponseDTO finalize(Integer id, OpportunityApprovalDTO dto) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        DhoOpportunityStatus finalizedStatus = statusRepository.findByName("Finalizada")
                .orElseThrow(() -> new RuntimeException("Status 'Finalizada' não encontrado"));

        opportunity.setOpportunityStatus(finalizedStatus);
        opportunity.setFinalizationJustification(dto.reason());
        return opportunityMapper.convertToDTO(opportunityRepository.save(opportunity), false);
    }

    @Transactional
    public OpportunityResponseDTO assignRecruiter(Integer id, OpportunityAssignRecruiterDTO dto) {
        log.info("[OpportunityWorkflowService] assignRecruiter - id: {}, recruiterId: {}", id, dto.recruiterId());
        
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada"));

        People recruiter = peopleRepository.findById(dto.recruiterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recrutador não encontrado"));

        opportunity.setResponsibleRecruiter(recruiter);
        Opportunity savedOpportunity = opportunityRepository.save(opportunity);
        
        return opportunityMapper.convertToDTO(savedOpportunity, false);
    }
}
