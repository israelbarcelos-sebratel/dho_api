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
              "!execution(public * br.com.sebratel.bff.dho.service.RecruitmentProcessService.get*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.RecruitmentProcessService.mapTo*(..))) || " +
              "(execution(public * br.com.sebratel.bff.dho.service.OpportunityService.*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.OpportunityService.get*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.OpportunityService.find*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.OpportunityService.findAll*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.OpportunityService.convertTo*(..))) || " +
              "(execution(public * br.com.sebratel.bff.dho.service.talentpool.TalentPoolService.*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.talentpool.TalentPoolService.get*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.talentpool.TalentPoolService.find*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.talentpool.TalentPoolService.findAll*(..)))")
    public void loggedActions() {}

    @Around("loggedActions()")
    public Object logAction(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String serviceName = joinPoint.getSignature().getDeclaringTypeName();
        Object[] args = joinPoint.getArgs();
        Integer entityId = (args.length > 0 && args[0] instanceof Integer) ? (Integer) args[0] : null;

        LocalDateTime startTime = LocalDateTime.now();
        long start = System.currentTimeMillis();
        String status = "SUCCESS";
        String errorMessage = null;
        Object result;

        try {
            result = joinPoint.proceed();
            if (entityId == null && result != null) {
                if (result instanceof RecruitmentProcessResponseDTO) entityId = ((RecruitmentProcessResponseDTO) result).getId();
                else if (result instanceof OpportunityResponseDTO) entityId = ((OpportunityResponseDTO) result).getId();
            }
            String humanAction = getHumanizedAction(serviceName, methodName, args, result);
            if (humanAction != null && entityId != null) {
                saveLog(serviceName, entityId, humanAction, startTime, LocalDateTime.now(), System.currentTimeMillis() - start, status, errorMessage);
            }
        } catch (Throwable throwable) {
            status = "FAILURE";
            errorMessage = throwable.getMessage();
            String humanAction = getHumanizedAction(serviceName, methodName, args, null);
            if (humanAction != null && entityId != null) {
                saveLog(serviceName, entityId, humanAction, startTime, LocalDateTime.now(), System.currentTimeMillis() - start, status, errorMessage);
            }
            throw throwable;
        }
        return result;
    }

    private String getHumanizedAction(String serviceName, String methodName, Object[] args, Object result) {
        if (serviceName.contains("OpportunityService")) {
            return switch (methodName) {
                case "create" -> "Criação da oportunidade";
                case "approve" -> "Oportunidade aprovada";
                case "refuse" -> "Oportunidade recusada";
                case "finalize" -> "Oportunidade finalizada";
                case "assignRecruiter" -> "Atribuição de recrutador à oportunidade";
                default -> null;
            };
        }
        if (serviceName.contains("RecruitmentProcessService")) {
            return switch (methodName) {
                case "create" -> "Candidato vinculado à oportunidade";
                case "approve" -> "Candidato aprovado";
                case "refuse" -> "Candidato reprovado";
                case "withdraw" -> "Candidato desistiu do processo";
                case "hire" -> "Contratação do candidato efetivada";
                case "moveToInterview" -> "Candidato movido para Entrevista";
                case "moveToTechnicalTest" -> "Candidato movido para Teste Técnico";
                case "moveToScreening" -> "Candidato movido para Triagem";
                case "moveToFinalDecision" -> "Candidato movido para Decisão Final";
                case "sendProposal" -> "Proposta enviada ao candidato";
                case "updateStage" -> "Etapa do processo atualizada";
                case "updateStatus" -> "Status do processo atualizado";
                case "managerDecision" -> {
                    if (args.length > 1 && args[1] instanceof br.com.sebratel.bff.dho.dto.ManagerDecisionDTO dto) {
                        yield dto.isApproved() ? "Gestor aprovou candidato" : "Gestor reprovou candidato";
                    }
                    yield "Decisão do gestor registrada";
                }
                case "candidateDecision" -> {
                    if (args.length > 1 && args[1] instanceof br.com.sebratel.bff.dho.dto.CandidateDecisionDTO dto) {
                        yield dto.isAccepted() ? "Candidato aceitou a proposta" : "Candidato recusou a proposta";
                    }
                    yield "Decisão do candidato registrada";
                }
                default -> null;
            };
        }
        if (serviceName.contains("TalentPoolService")) {
            return switch (methodName) {
                case "addToPool" -> "Pessoa adicionada ao Banco de Talentos";
                case "updatePoolEntry" -> "Registro no Banco de Talentos atualizado";
                case "removeFromPool" -> "Pessoa removida do Banco de Talentos";
                default -> null;
            };
        }
        return null;
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
                .actionName(actionName)
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
