package br.com.sebratel.bff.dho.aspect;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessLogRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Duration;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RecruitmentProcessLoggingAspect {

    private final RecruitmentProcessLogRepository logRepository;
    private final RecruitmentProcessRepository recruitmentProcessRepository;

    @Pointcut("execution(public * br.com.sebratel.bff.dho.service.RecruitmentProcessService.*(..)) && " +
              "!execution(public * br.com.sebratel.bff.dho.service.RecruitmentProcessService.get*(..))")
    public void recruitmentProcessActions() {}

    @Around("recruitmentProcessActions()")
    public Object logAction(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Integer processId = null;

        if (args.length > 0 && args[0] instanceof Integer) {
            processId = (Integer) args[0];
        }

        LocalDateTime startTime = LocalDateTime.now();
        long start = System.currentTimeMillis();
        
        String status = "SUCCESS";
        String errorMessage = null;
        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            status = "FAILURE";
            errorMessage = throwable.getMessage();
            throw throwable;
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            LocalDateTime endTime = LocalDateTime.now();

            if (processId != null) {
                try {
                    saveLog(processId, methodName, startTime, endTime, executionTime, status, errorMessage);
                } catch (Exception e) {
                    log.error("Failed to save recruitment process log", e);
                }
            }
        }

        return result;
    }

    private void saveLog(Integer processId, String actionName, LocalDateTime startTime, LocalDateTime endTime, 
                         long durationMs, String status, String errorMessage) {
        recruitmentProcessRepository.findById(processId).ifPresent(process -> {
            RecruitmentProcessLog recruitmentLog = RecruitmentProcessLog.builder()
                    .recruitmentProcess(process)
                    .actionName(actionName.toUpperCase())
                    .startTime(startTime)
                    .endTime(endTime)
                    .durationMs(durationMs)
                    .status(status)
                    .errorMessage(errorMessage != null && errorMessage.length() > 2000 ? 
                                 errorMessage.substring(0, 2000) : errorMessage)
                    .build();
            logRepository.save(recruitmentLog);
        });
    }
}
