package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.DhoDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DhoDocumentRepository extends JpaRepository<DhoDocument, Integer> {
    List<DhoDocument> findByCandidateId(Integer candidateId);
}
