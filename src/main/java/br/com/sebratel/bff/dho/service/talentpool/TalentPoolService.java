package br.com.sebratel.bff.dho.service.talentpool;

import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import br.com.sebratel.bff.dho.dto.TalentPoolRequestDTO;
import br.com.sebratel.bff.dho.dto.TalentPoolResponseDTO;

import java.util.List;

public interface TalentPoolService {
    TalentPoolResponseDTO addToPool(TalentPoolRequestDTO request);
    TalentPoolResponseDTO updatePoolEntry(Integer id, TalentPoolRequestDTO request);
    List<TalentPoolResponseDTO> findAll();
    TalentPoolResponseDTO findById(Integer id);
    void removeFromPool(Integer id);
    List<RecruitmentProcessResponseDTO> findProcessesByTalentPoolId(Integer id);
}
