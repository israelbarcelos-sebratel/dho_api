package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.SuggestionRequestDTO;
import br.com.sebratel.bff.dho.dto.SuggestionResponseDTO;
import br.com.sebratel.bff.dho.dto.VoteRequestDTO;
import br.com.sebratel.bff.dho.service.SuggestionService;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoPermissionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SuggestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SuggestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SuggestionService suggestionService;

    @MockBean
    private PeopleRepository peopleRepository;

    @MockBean
    private DhoRoleRepository roleRepository;

    @MockBean
    private DhoPermissionRepository permissionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_ShouldReturnList() throws Exception {
        SuggestionResponseDTO response = new SuggestionResponseDTO(1L, "Title", "Desc", "email@test.com", 0L, 0L);
        when(suggestionService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Title"));
    }

    @Test
    void getById_ShouldReturnSuggestion() throws Exception {
        SuggestionResponseDTO response = new SuggestionResponseDTO(1L, "Title", "Desc", "email@test.com", 0L, 0L);
        when(suggestionService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/suggestions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void create_WithValidData_ShouldReturnCreated() throws Exception {
        SuggestionRequestDTO request = new SuggestionRequestDTO("Title", "Desc", "email@test.com");
        SuggestionResponseDTO response = new SuggestionResponseDTO(1L, "Title", "Desc", "email@test.com", 0L, 0L);
        when(suggestionService.create(any(SuggestionRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void create_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        SuggestionRequestDTO request = new SuggestionRequestDTO("", "", "invalido");

        mockMvc.perform(post("/suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_WithValidData_ShouldReturnOk() throws Exception {
        SuggestionRequestDTO request = new SuggestionRequestDTO("Updated Title", "Updated Desc", "email@test.com");
        SuggestionResponseDTO response = new SuggestionResponseDTO(1L, "Updated Title", "Updated Desc", "email@test.com", 0L, 0L);
        when(suggestionService.update(eq(1L), any(SuggestionRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/suggestions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        doNothing().when(suggestionService).delete(1L);

        mockMvc.perform(delete("/suggestions/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void vote_WithValidData_ShouldReturnCreated() throws Exception {
        VoteRequestDTO request = new VoteRequestDTO("voter@test.com", 1);
        doNothing().when(suggestionService).vote(eq(1L), any(VoteRequestDTO.class));

        mockMvc.perform(post("/suggestions/1/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
