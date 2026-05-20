package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Suggestion;
import br.com.sebratel.bff.dho.domain.entity.SuggestionVote;
import br.com.sebratel.bff.dho.domain.repository.SuggestionRepository;
import br.com.sebratel.bff.dho.domain.repository.SuggestionVoteRepository;
import br.com.sebratel.bff.dho.dto.SuggestionRequestDTO;
import br.com.sebratel.bff.dho.dto.SuggestionResponseDTO;
import br.com.sebratel.bff.dho.dto.VoteRequestDTO;
import br.com.sebratel.bff.dho.util.EncryptionUtil;
import br.com.sebratel.bff.dho.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final SuggestionVoteRepository suggestionVoteRepository;
    private final EncryptionUtil encryptionUtil;
    private final HashUtil hashUtil;

    public List<SuggestionResponseDTO> findAll() {
        return suggestionRepository.findAll()
                .stream()
                .map(SuggestionResponseDTO::fromEntity)
                .toList();
    }

    public SuggestionResponseDTO findById(Long id) {
        return suggestionRepository.findById(id)
                .map(SuggestionResponseDTO::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sugestão não encontrada"));
    }

    public SuggestionResponseDTO create(SuggestionRequestDTO dto, String email) {
        Suggestion suggestion = Suggestion.builder()
                .title(dto.title())
                .description(dto.description())
                .email(encryptionUtil.encrypt(email))
                .emailHash(hashUtil.hash(email))
                .build();
        
        return SuggestionResponseDTO.fromEntity(suggestionRepository.save(suggestion));
    }

    public SuggestionResponseDTO update(Long id, SuggestionRequestDTO dto, String email) {
        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sugestão não encontrada"));
        
        suggestion.setTitle(dto.title());
        suggestion.setDescription(dto.description());
        suggestion.setEmail(encryptionUtil.encrypt(email));
        suggestion.setEmailHash(hashUtil.hash(email));
        
        return SuggestionResponseDTO.fromEntity(suggestionRepository.save(suggestion));
    }

    public void delete(Long id) {
        if (!suggestionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sugestão não encontrada");
        }
        suggestionRepository.deleteById(id);
    }

    public void vote(Long id, VoteRequestDTO dto, String email) {
        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sugestão não encontrada"));

        String voterEmailHash = hashUtil.hash(email);
        if (voterEmailHash.equals(suggestion.getEmailHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O usuário não pode votar na sua própria sugestão");
        }

        Optional<SuggestionVote> existingVote = suggestionVoteRepository.findBySuggestionAndEmail(suggestion, voterEmailHash);
        
        String requestedVoteType = dto.vote() > 0 ? "POSITIVO" : "NEGATIVO";

        if (existingVote.isPresent()) {
            SuggestionVote vote = existingVote.get();
            if (vote.getVote().equals(requestedVoteType)) {
                // Se o voto enviado é igual ao atual, remove o voto (altera para SEM_VOTO)
                vote.setVote("SEM_VOTO");
            } else {
                // Se o voto é diferente (ex: era POSITIVO e enviou NEGATIVO), atualiza para o novo
                vote.setVote(requestedVoteType);
            }
            suggestionVoteRepository.save(vote);
        } else {
            // Se não existe voto, cria um novo
            SuggestionVote vote = SuggestionVote.builder()
                    .suggestion(suggestion)
                    .email(voterEmailHash)
                    .vote(requestedVoteType)
                    .build();
            suggestionVoteRepository.save(vote);
        }
    }
}
