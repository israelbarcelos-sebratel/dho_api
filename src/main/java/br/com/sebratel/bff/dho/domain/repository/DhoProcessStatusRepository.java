package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DhoProcessStatusRepository extends JpaRepository<DhoProcessStatus, Integer> {
    Optional<DhoProcessStatus> findByName(String name);
}
