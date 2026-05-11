package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.Suggestion;
import br.com.sebratel.bff.dho.domain.entity.SuggestionVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuggestionVoteRepository extends JpaRepository<SuggestionVote, Long> {
    boolean existsBySuggestionAndEmail(Suggestion suggestion, String email);
    Optional<SuggestionVote> findBySuggestionAndEmail(Suggestion suggestion, String email);
}
