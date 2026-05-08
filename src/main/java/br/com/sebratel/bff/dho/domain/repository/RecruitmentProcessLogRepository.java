package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitmentProcessLogRepository extends JpaRepository<RecruitmentProcessLog, Integer> {
    List<RecruitmentProcessLog> findByRecruitmentProcessId(Integer recruitmentProcessId);
}
