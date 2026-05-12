package br.com.sebratel.bff.dho.domain.entity;

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
@Table(name = "recruitment_process_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentProcessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "recruitment_process_id")
    private RecruitmentProcess recruitmentProcess;

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;
}
