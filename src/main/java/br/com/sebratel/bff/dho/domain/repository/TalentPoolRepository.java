package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.talentpool.TalentPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TalentPoolRepository extends JpaRepository<TalentPool, Integer> {
    Optional<TalentPool> findByPersonId(Integer personId);
    boolean existsByPersonId(Integer personId);
}
