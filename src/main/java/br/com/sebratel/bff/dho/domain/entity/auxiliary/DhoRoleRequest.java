package br.com.sebratel.bff.dho.domain.entity.auxiliary;

import br.com.sebratel.bff.dho.domain.entity.People;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DhoRoleRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "people_id")
    private People person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private DhoRole role;

    @Column(name = "status")
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;
}
