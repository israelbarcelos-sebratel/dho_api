package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Suggestion;
import br.com.sebratel.bff.dho.domain.repository.SuggestionRepository;
import br.com.sebratel.bff.dho.domain.repository.SuggestionVoteRepository;
import br.com.sebratel.bff.dho.dto.SuggestionRequestDTO;
import br.com.sebratel.bff.dho.dto.SuggestionResponseDTO;
import br.com.sebratel.bff.dho.dto.VoteRequestDTO;
import br.com.sebratel.bff.dho.util.EncryptionUtil;
import br.com.sebratel.bff.dho.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock
    private SuggestionRepository suggestionRepository;

    @Mock
    private SuggestionVoteRepository suggestionVoteRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private HashUtil hashUtil;

    @InjectMocks
    private SuggestionService suggestionService;

    private Suggestion suggestion;
    private SuggestionRequestDTO suggestionRequestDTO;
    private final String testEmail = "test@sebratel.com.br";

    @BeforeEach
    void setUp() {
        suggestion = Suggestion.builder()
                .id(1L)
                .title("Test Title")
                .description("Test Description")
                .email("encrypted-email")
                .emailHash("hashed-email")
                .votes(new ArrayList<>())
                .build();

        suggestionRequestDTO = new SuggestionRequestDTO("Test Title", "Test Description");
        
        lenient().when(encryptionUtil.encrypt(anyString())).thenReturn("encrypted-email");
        lenient().when(hashUtil.hash(anyString())).thenReturn("hashed-email");
    }

    @Test
    void findAll_ShouldReturnList() {
        when(suggestionRepository.findAll()).thenReturn(List.of(suggestion));

        List<SuggestionResponseDTO> result = suggestionService.findAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(suggestion.getTitle(), result.get(0).title());
        verify(suggestionRepository, times(1)).findAll();
    }

    @Test
    void findById_WhenIdExists_ShouldReturnSuggestion() {
        when(suggestionRepository.findById(1L)).thenReturn(Optional.of(suggestion));

        SuggestionResponseDTO result = suggestionService.findById(1L);

        assertNotNull(result);
        assertEquals(suggestion.getTitle(), result.title());
        verify(suggestionRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenIdDoesNotExist_ShouldThrowException() {
        when(suggestionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> suggestionService.findById(1L));
        verify(suggestionRepository, times(1)).findById(1L);
    }

    @Test
    void create_ShouldReturnSavedSuggestion() {
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(suggestion);

        SuggestionResponseDTO result = suggestionService.create(suggestionRequestDTO, testEmail);

        assertNotNull(result);
        assertEquals(suggestion.getTitle(), result.title());
        verify(encryptionUtil, times(1)).encrypt(testEmail);
        verify(suggestionRepository, times(1)).save(any(Suggestion.class));
    }

    @Test
    void update_WhenIdExists_ShouldReturnUpdatedSuggestion() {
        when(suggestionRepository.findById(1L)).thenReturn(Optional.of(suggestion));
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(suggestion);

        SuggestionResponseDTO result = suggestionService.update(1L, suggestionRequestDTO, testEmail);

        assertNotNull(result);
        assertEquals(suggestion.getTitle(), result.title());
        verify(encryptionUtil, times(1)).encrypt(testEmail);
        verify(suggestionRepository, times(1)).findById(1L);
        verify(suggestionRepository, times(1)).save(any(Suggestion.class));
    }

    @Test
    void update_WhenIdDoesNotExist_ShouldThrowException() {
        when(suggestionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> suggestionService.update(1L, suggestionRequestDTO, testEmail));
        verify(suggestionRepository, times(1)).findById(1L);
        verify(suggestionRepository, never()).save(any());
    }

    @Test
    void delete_WhenIdExists_ShouldDelete() {
        when(suggestionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(suggestionRepository).deleteById(1L);

        suggestionService.delete(1L);

        verify(suggestionRepository, times(1)).existsById(1L);
        verify(suggestionRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_WhenIdDoesNotExist_ShouldThrowException() {
        when(suggestionRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> suggestionService.delete(1L));
        verify(suggestionRepository, times(1)).existsById(1L);
        verify(suggestionRepository, never()).deleteById(any());
    }

    @Test
    void vote_ShouldSaveVote() {
        String voterEmail = "voter@sebratel.com.br";
        VoteRequestDTO voteDTO = new VoteRequestDTO(1);
        when(suggestionRepository.findById(1L)).thenReturn(Optional.of(suggestion));
        when(hashUtil.hash(voterEmail)).thenReturn("different-hash");
        when(suggestionVoteRepository.findBySuggestionAndEmail(any(), anyString())).thenReturn(Optional.empty());

        suggestionService.vote(1L, voteDTO, voterEmail);

        verify(hashUtil, atLeastOnce()).hash(voterEmail);
        verify(suggestionVoteRepository, times(1)).save(any());
    }

    @Test
    void vote_WhenUserVotesOnOwnSuggestion_ShouldThrowException() {
        VoteRequestDTO voteDTO = new VoteRequestDTO(1);
        when(suggestionRepository.findById(1L)).thenReturn(Optional.of(suggestion));
        when(hashUtil.hash(testEmail)).thenReturn("hashed-email");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> suggestionService.vote(1L, voteDTO, testEmail));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("O usuário não pode votar na sua própria sugestão", exception.getReason());
        verify(suggestionVoteRepository, never()).save(any());
    }

}
