package br.com.sebratel.bff.dho.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recruitment_process_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentProcessItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "recruitment_process_id")
    private RecruitmentProcess recruitmentProcess;

    @Column(name = "comment", length = 2000)
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;
}
