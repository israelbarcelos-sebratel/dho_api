package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Integer> {

    long countByOpportunityStatusNameNotIn(List<String> statusNames);

    List<Opportunity> findByRequesterEmail(String email);

    List<Opportunity> findByOpportunityStatusName(String statusName);

}
