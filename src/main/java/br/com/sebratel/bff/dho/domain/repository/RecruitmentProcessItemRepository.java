package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitmentProcessItemRepository extends JpaRepository<RecruitmentProcessItem, Integer> {
    List<RecruitmentProcessItem> findByRecruitmentProcessIdOrderByCreatedAtDesc(Integer recruitmentProcessId);
}
