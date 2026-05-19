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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

        mockMvc.perform(get("/api/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("0.id").value(1L))
                .andExpect(jsonPath("0.title").value("Title"));
    }

    @Test
    void getById_ShouldReturnSuggestion() throws Exception {
        SuggestionResponseDTO response = new SuggestionResponseDTO(1L, "Title", "Desc", "email@test.com", 0L, 0L);
        when(suggestionService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/suggestions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void create_WithValidData_ShouldReturnCreated() throws Exception {
        SuggestionRequestDTO request = new SuggestionRequestDTO("Title", "Desc");
        SuggestionResponseDTO response = new SuggestionResponseDTO(1L, "Title", "Desc", "email@test.com", 0L, 0L);
        when(suggestionService.create(any(SuggestionRequestDTO.class), anyString())).thenReturn(response);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("email@test.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        mockMvc.perform(post("/api/suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void create_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        SuggestionRequestDTO request = new SuggestionRequestDTO("", "");

        mockMvc.perform(post("/api/suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_WithValidData_ShouldReturnOk() throws Exception {
        SuggestionRequestDTO request = new SuggestionRequestDTO("Updated Title", "Updated Desc");
        SuggestionResponseDTO response = new SuggestionResponseDTO(1L, "Updated Title", "Updated Desc", "email@test.com", 0L, 0L);
        when(suggestionService.update(eq(1L), any(SuggestionRequestDTO.class), anyString())).thenReturn(response);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("email@test.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        mockMvc.perform(put("/api/suggestions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        doNothing().when(suggestionService).delete(1L);

        mockMvc.perform(delete("/api/suggestions/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void vote_WithValidData_ShouldReturnCreated() throws Exception {
        VoteRequestDTO request = new VoteRequestDTO(1);
        doNothing().when(suggestionService).vote(eq(1L), any(VoteRequestDTO.class), anyString());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("voter@test.com", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        mockMvc.perform(post("/api/suggestions/1/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isCreated());
    }
}
