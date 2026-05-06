package br.com.sebratel.bff.dho.domain.entity.auxiliary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "process_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DhoProcessStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "process_status_name")
    private String name;

    @Column(name = "process_status_description")
    private String description;
}
