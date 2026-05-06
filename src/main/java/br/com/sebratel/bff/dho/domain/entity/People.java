package br.com.sebratel.bff.dho.domain.entity;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoEducation;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRecruitmentSource;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoResignationMotivation;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoResignationType;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoSituation;
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
@Table(name = "people")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class People {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "registration_number")
    private Integer registrationNumber;

    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String cpf;

    private String rg;

    @Column(name = "date_birth")
    private LocalDateTime dateBirth;

    private String sex;

    private Boolean replacement;

    @Column(name = "recruitment_data")
    private LocalDateTime recruitmentData;

    @Column(name = "admission_date")
    private LocalDateTime admissionDate;

    private String observations;

    @Column(name = "professional_references")
    private String professionalReferences;

    @Column(name = "collaborator_knowledge")
    private String collaboratorKnowledge;

    @Column(name = "labor_lawsuit")
    private String laborLawsuit;

    @Column(name = "criminal_background")
    private Boolean criminalBackground;

    @Column(name = "external_link")
    private String externalLink;

    @Column(name = "mindsight_link")
    private String mindsightLink;

    @Column(name = "cis_link")
    private String cisLink;

    @ManyToOne
    @JoinColumn(name = "id_resignation_motivation")
    private DhoResignationMotivation resignationMotivation;

    @ManyToOne
    @JoinColumn(name = "id_resignation_type")
    private DhoResignationType resignationType;

    @ManyToOne
    @JoinColumn(name = "id_education")
    private DhoEducation education;

    @ManyToOne
    @JoinColumn(name = "id_situation")
    private DhoSituation situation;

    @ManyToOne
    @JoinColumn(name = "id_recruitment_source")
    private DhoRecruitmentSource recruitmentSource;
}
