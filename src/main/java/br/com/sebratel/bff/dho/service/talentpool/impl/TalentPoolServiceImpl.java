package br.com.sebratel.bff.dho.service.talentpool.impl;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoPosition;
import br.com.sebratel.bff.dho.domain.entity.talentpool.TalentPool;
import br.com.sebratel.bff.dho.domain.repository.DhoPositionRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.domain.repository.TalentPoolRepository;
import br.com.sebratel.bff.dho.dto.TalentPoolRequestDTO;
import br.com.sebratel.bff.dho.dto.TalentPoolResponseDTO;
import br.com.sebratel.bff.dho.service.talentpool.TalentPoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TalentPoolServiceImpl implements TalentPoolService {

    private final TalentPoolRepository talentPoolRepository;
    private final PeopleRepository peopleRepository;
    private final DhoPositionRepository positionRepository;

    @Override
    public TalentPoolResponseDTO addToPool(TalentPoolRequestDTO request) {
        if (talentPoolRepository.existsByPersonId(request.getPeopleId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pessoa já está no banco de talentos");
        }

        People person = peopleRepository.findById(request.getPeopleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada"));

        Set<DhoPosition> positions = new HashSet<>(positionRepository.findAllById(request.getPositionIds()));

        TalentPool entry = TalentPool.builder()
                .person(person)
                .observations(request.getObservations())
                .suggestedPositions(positions)
                .build();

        return mapToResponse(talentPoolRepository.save(entry));
    }

    @Override
    public TalentPoolResponseDTO updatePoolEntry(Integer id, TalentPoolRequestDTO request) {
        TalentPool entry = talentPoolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado no banco de talentos"));

        if (request.getObservations() != null) {
            entry.setObservations(request.getObservations());
        }

        if (request.getPositionIds() != null) {
            Set<DhoPosition> positions = new HashSet<>(positionRepository.findAllById(request.getPositionIds()));
            entry.setSuggestedPositions(positions);
        }

        return mapToResponse(talentPoolRepository.save(entry));
    }

    @Override
    public List<TalentPoolResponseDTO> findAll() {
        return talentPoolRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TalentPoolResponseDTO findById(Integer id) {
        return talentPoolRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado"));
    }

    @Override
    public void removeFromPool(Integer id) {
        if (!talentPoolRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado");
        }
        talentPoolRepository.deleteById(id);
    }

    private TalentPoolResponseDTO mapToResponse(TalentPool entry) {
        return TalentPoolResponseDTO.builder()
                .id(entry.getId())
                .peopleId(entry.getPerson().getId())
                .name(entry.getPerson().getName())
                .email(entry.getPerson().getEmail())
                .phoneNumber(entry.getPerson().getPhoneNumber())
                .observations(entry.getObservations())
                .suggestedPositions(entry.getSuggestedPositions().stream()
                        .map(DhoPosition::getName)
                        .collect(Collectors.toSet()))
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}
