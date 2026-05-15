package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoBaseOrigin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DhoBaseOriginRepository extends JpaRepository<DhoBaseOrigin, Integer> {
    Optional<DhoBaseOrigin> findByName(String name);
}
