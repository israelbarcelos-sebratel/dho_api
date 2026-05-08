package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitmentProcessRepository extends JpaRepository<RecruitmentProcess, Integer> {
    List<RecruitmentProcess> findByOpportunityId(Integer opportunityId);
    List<RecruitmentProcess> findByProcessStatusNameIn(List<String> statusNames);
}
