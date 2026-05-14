package br.com.sebratel.bff.dho.domain.entity.talentpool;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoPosition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "talent_pool")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TalentPool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "people_id", nullable = false)
    private People person;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @ManyToMany
    @JoinTable(
        name = "talent_pool_positions",
        joinColumns = @JoinColumn(name = "talent_pool_id"),
        inverseJoinColumns = @JoinColumn(name = "position_id")
    )
    private Set<DhoPosition> suggestedPositions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
