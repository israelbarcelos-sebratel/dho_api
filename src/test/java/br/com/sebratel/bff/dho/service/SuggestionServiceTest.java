package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Suggestion;
import br.com.sebratel.bff.dho.domain.repository.SuggestionRepository;
import br.com.sebratel.bff.dho.domain.repository.SuggestionVoteRepository;
import br.com.sebratel.bff.dho.dto.SuggestionRequestDTO;
import br.com.sebratel.bff.dho.dto.SuggestionResponseDTO;
import br.com.sebratel.bff.dho.dto.VoteRequestDTO;
import br.com.sebratel.bff.dho.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

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

    @InjectMocks
    private SuggestionService suggestionService;

    private Suggestion suggestion;
    private SuggestionRequestDTO suggestionRequestDTO;

    @BeforeEach
    void setUp() {
        suggestion = Suggestion.builder()
                .id(1L)
                .title("Test Title")
                .description("Test Description")
                .email("encrypted-email")
                .votes(new ArrayList<>())
                .build();

        suggestionRequestDTO = new SuggestionRequestDTO("Test Title", "Test Description", "test@sebratel.com.br");
        
        lenient().when(encryptionUtil.encrypt(anyString())).thenReturn("encrypted-email");
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

        SuggestionResponseDTO result = suggestionService.create(suggestionRequestDTO);

        assertNotNull(result);
        assertEquals(suggestion.getTitle(), result.title());
        verify(encryptionUtil, times(1)).encrypt("test@sebratel.com.br");
        verify(suggestionRepository, times(1)).save(any(Suggestion.class));
    }

    @Test
    void update_WhenIdExists_ShouldReturnUpdatedSuggestion() {
        when(suggestionRepository.findById(1L)).thenReturn(Optional.of(suggestion));
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(suggestion);

        SuggestionResponseDTO result = suggestionService.update(1L, suggestionRequestDTO);

        assertNotNull(result);
        assertEquals(suggestion.getTitle(), result.title());
        verify(encryptionUtil, times(1)).encrypt("test@sebratel.com.br");
        verify(suggestionRepository, times(1)).findById(1L);
        verify(suggestionRepository, times(1)).save(any(Suggestion.class));
    }

    @Test
    void update_WhenIdDoesNotExist_ShouldThrowException() {
        when(suggestionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> suggestionService.update(1L, suggestionRequestDTO));
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
        VoteRequestDTO voteDTO = new VoteRequestDTO("voter@sebratel.com.br", 1);
        when(suggestionRepository.findById(1L)).thenReturn(Optional.of(suggestion));

        suggestionService.vote(1L, voteDTO);

        verify(encryptionUtil, times(1)).encrypt("voter@sebratel.com.br");
        verify(suggestionVoteRepository, times(1)).save(any());
    }
}
