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
@Table(name = "base_origin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DhoBaseOrigin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "base_origin_name")
    private String name;

    @Column(name = "base_origin_description")
    private String description;
}
