package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DhoPositionRepository extends JpaRepository<DhoPosition, Integer> {
}
