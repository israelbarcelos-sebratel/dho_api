package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoPermission;
import br.com.sebratel.bff.dho.domain.enums.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DhoPermissionRepository extends JpaRepository<DhoPermission, Integer> {
    Optional<DhoPermission> findByName(Permission name);
}
