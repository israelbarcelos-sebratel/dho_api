package br.com.sebratel.bff.dho.aspect;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.People;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessLogRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RecruitmentProcessLoggingAspect {

    private final RecruitmentProcessLogRepository logRepository;
    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final OpportunityRepository opportunityRepository;

    @Pointcut("(execution(public * br.com.sebratel.bff.dho.service.RecruitmentProcessService.*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.RecruitmentProcessService.get*(..))) || " +
              "(execution(public * br.com.sebratel.bff.dho.service.OpportunityService.*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.OpportunityService.get*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.OpportunityService.find*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.OpportunityService.findAll*(..)))")
    public void loggedActions() {}

    @Around("loggedActions()")
    public Object logAction(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Integer entityId = null;

        // Try to get ID from first argument if it's an Integer
        if (args.length > 0 && args[0] instanceof Integer) {
            entityId = (Integer) args[0];
        }

        LocalDateTime startTime = LocalDateTime.now();
        long start = System.currentTimeMillis();
        
        String status = "SUCCESS";
        String errorMessage = null;
        Object result;

        try {
            result = joinPoint.proceed();
            
            // If it's a create method, we get the ID from the returned DTO
            if (entityId == null && result != null) {
                if (result instanceof RecruitmentProcessResponseDTO) {
                    entityId = ((RecruitmentProcessResponseDTO) result).getId();
                } else if (result instanceof OpportunityResponseDTO) {
                    entityId = ((OpportunityResponseDTO) result).getId();
                }
            }
        } catch (Throwable throwable) {
            status = "FAILURE";
            errorMessage = throwable.getMessage();
            throw throwable;
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            LocalDateTime endTime = LocalDateTime.now();

            if (entityId != null) {
                try {
                    saveLog(joinPoint.getSignature().getDeclaringTypeName(), entityId, methodName, startTime, endTime, executionTime, status, errorMessage);
                } catch (Exception e) {
                    log.error("Failed to save action log", e);
                }
            }
        }

        return result;
    }

    private void saveLog(String serviceName, Integer id, String actionName, LocalDateTime startTime, LocalDateTime endTime, 
                         long durationMs, String status, String errorMessage) {
        
        Opportunity opportunity = null;
        People candidate = null;

        if (serviceName.contains("RecruitmentProcessService")) {
            RecruitmentProcess process = recruitmentProcessRepository.findById(id).orElse(null);
            if (process != null) {
                opportunity = process.getOpportunity();
                candidate = process.getCandidate();
            }
        } else if (serviceName.contains("OpportunityService")) {
            opportunity = opportunityRepository.findById(id).orElse(null);
            // In OpportunityService.create, the candidate might be in the DTO, 
            // but for simple logging of the opportunity itself, candidate is null.
        }

        if (opportunity != null) {
            RecruitmentProcessLog recruitmentLog = RecruitmentProcessLog.builder()
                    .opportunity(opportunity)
                    .candidate(candidate)
                    .actionName(actionName.toUpperCase())
                    .startTime(startTime)
                    .endTime(endTime)
                    .durationMs(durationMs)
                    .status(status)
                    .errorMessage(errorMessage != null && errorMessage.length() > 2000 ? 
                                 errorMessage.substring(0, 2000) : errorMessage)
                    .build();
            logRepository.save(recruitmentLog);
        }
    }
}
