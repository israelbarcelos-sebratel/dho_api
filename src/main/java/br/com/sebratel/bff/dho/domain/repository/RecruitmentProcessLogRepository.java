package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitmentProcessLogRepository extends JpaRepository<RecruitmentProcessLog, Integer> {
    List<RecruitmentProcessLog> findByActionNameInAndStatusAndStartTimeAfter(java.util.Collection<String> actionNames, String status, LocalDateTime startTime);

    List<RecruitmentProcessLog> findByActionNameAndStatusAndStartTimeAfter(String actionName, String status, LocalDateTime startTime);

    List<RecruitmentProcessLog> findByRecruitmentProcessIdOrderByStartTimeDesc(Integer processId);

    List<RecruitmentProcessLog> findByRecruitmentProcessOpportunityIdOrderByStartTimeDesc(Integer opportunityId);
}
