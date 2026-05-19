package br.com.sebratel.bff.dho.aspect;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.People;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStageRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStatusRepository;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStage;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.List;
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
        
        RecruitmentProcess process = null;

        if (serviceName.contains("RecruitmentProcessService")) {
            process = recruitmentProcessRepository.findById(id).orElse(null);
        } else if (serviceName.contains("OpportunityService")) {
            // If it's an opportunity, we might want to log it against all its processes
            // or maybe just the "main" one. Given the requirement to update opportunity log,
            // we should probably find the relevant processes.
            List<RecruitmentProcess> processes = recruitmentProcessRepository.findByOpportunityId(id);
            // For simplicity and to match the "process log" nature, we'll log it for each process 
            // of this opportunity if they exist.
            for (RecruitmentProcess rp : processes) {
                saveSingleLog(rp, actionName, startTime, endTime, durationMs, status, errorMessage);
            }
            return;
        }

        if (process != null) {
            saveSingleLog(process, actionName, startTime, endTime, durationMs, status, errorMessage);
        }
    }

    private void saveSingleLog(RecruitmentProcess process, String actionName, LocalDateTime startTime, LocalDateTime endTime,
                               long durationMs, String status, String errorMessage) {
        String executedBy = getExecutedBy();
        RecruitmentProcessLog recruitmentLog = RecruitmentProcessLog.builder()
                .recruitmentProcess(process)
                .actionName(actionName.toUpperCase())
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .status(status)
                .errorMessage(errorMessage != null && errorMessage.length() > 2000 ? 
                             errorMessage.substring(0, 2000) : errorMessage)
                .executedBy(executedBy)
                .build();
        logRepository.save(recruitmentLog);
    }

    private String getExecutedBy() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                String name = jwt.getClaimAsString("name");
                String email = jwt.getClaimAsString("email");
                if (name != null && email != null) {
                    return String.format("%s(%s)", name, email);
                }
                return auth.getName();
            }
            return auth.getName();
        }
        return "SYSTEM";
    }
}
