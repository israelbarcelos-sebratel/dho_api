package br.com.sebratel.bff.dho.domain.entity;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.*;
import jakarta.persistence.Column;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "opportunities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "open_opportunity_date")
    private LocalDateTime openOpportunityDate;

    @ManyToOne
    @JoinColumn(name = "people_id")
    private People candidate;

    @ManyToOne
    @JoinColumn(name = "position_id")
    private DhoPosition position;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private DhoTeam team;

    @ManyToOne
    @JoinColumn(name = "departament_id")
    private DhoDepartment department;

    @ManyToOne
    @JoinColumn(name = "opportunity_motive_id")
    private DhoOpportunityMotive opportunityMotive;

    @ManyToOne
    @JoinColumn(name = "replaced_person_id")
    private People replacedPerson;

    @ManyToOne
    @JoinColumn(name = "base_origin_id")
    private DhoBaseOrigin baseOrigin;

    @ManyToOne
    @JoinColumn(name = "opportunity_status_id")
    private DhoOpportunityStatus opportunityStatus;

    @ManyToOne
    @JoinColumn(name = "id_process_stage")
    private DhoProcessStage processStage;

    @ManyToOne
    @JoinColumn(name = "id_process_status")
    private DhoProcessStatus processStatus;

    @Column(name = "deadline_sla_days")
    private Integer deadlineSlaDays;

    @Column(name = "accept_date")
    private LocalDateTime acceptDate;

    @ManyToOne
    @JoinColumn(name = "responsible_recruiter_id")
    private People responsibleRecruiter;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private People requester;

    private String observations;

    @Column(name = "refusal_justification", length = 1000)
    private String refusalJustification;

    @Column(name = "finalization_justification", length = 1000)
    private String finalizationJustification;

    @Column(name = "work_schedule")
    private String workSchedule;

    @Column(name = "hard_skills", length = 2000)
    private String hardSkills;

    @Column(name = "soft_skills", length = 2000)
    private String softSkills;
}
