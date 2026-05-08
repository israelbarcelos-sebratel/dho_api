package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoOpportunityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DhoOpportunityStatusRepository extends JpaRepository<DhoOpportunityStatus, Integer> {
    Optional<DhoOpportunityStatus> findByName(String name);
}
