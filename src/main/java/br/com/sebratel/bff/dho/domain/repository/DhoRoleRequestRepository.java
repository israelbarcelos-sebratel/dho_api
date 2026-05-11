package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRoleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DhoRoleRequestRepository extends JpaRepository<DhoRoleRequest, Integer> {
    @Query("SELECT r FROM DhoRoleRequest r JOIN FETCH r.person p LEFT JOIN FETCH p.roles JOIN FETCH r.role WHERE r.id = :id")
    java.util.Optional<DhoRoleRequest> findByIdWithPersonAndRoles(@org.springframework.data.repository.query.Param("id") Integer id);

    List<DhoRoleRequest> findByStatus(String status);

    boolean existsByPersonAndStatus(People person, String status);
}
