package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.People;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeopleRepository extends JpaRepository<People, Integer> {
    List<People> findByRolesName(String roleName);
    
    @Query("SELECT p FROM People p LEFT JOIN FETCH p.roles r LEFT JOIN FETCH r.permissions WHERE p.email = :email")
    Optional<People> findByEmail(@Param("email") String email);
}
