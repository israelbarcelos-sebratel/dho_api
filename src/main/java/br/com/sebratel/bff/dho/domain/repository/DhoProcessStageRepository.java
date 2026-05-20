package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DhoProcessStageRepository extends JpaRepository<DhoProcessStage, Integer> {
    Optional<DhoProcessStage> findByName(String name);
    Optional<DhoProcessStage> findByNameIgnoreCase(String name);
}
