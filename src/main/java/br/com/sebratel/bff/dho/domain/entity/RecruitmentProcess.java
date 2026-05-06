package br.com.sebratel.bff.dho.domain.entity;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStage;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRecruitmentSource;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoSituation;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recruitment_process")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "people_id")
    private People candidate;

    @ManyToOne
    @JoinColumn(name = "opportunity_id")
    private Opportunity opportunity;

    @ManyToOne
    @JoinColumn(name = "process_status_id")
    private DhoProcessStatus processStatus;

    @ManyToOne
    @JoinColumn(name = "process_stage_id")
    private DhoProcessStage processStage;

    @ManyToOne
    @JoinColumn(name = "recruitment_source_id")
    private DhoRecruitmentSource recruitmentSource;

    @ManyToOne
    @JoinColumn(name = "situation_id")
    private DhoSituation situation;
}
