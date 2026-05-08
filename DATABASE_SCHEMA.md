# Database Schema Documentation

## Connection Details
- **Driver:** `org.mariadb.jdbc.Driver`
- **URL:** `jdbc:mariadb://localhost:3306/DHO_Application`
- **Username:** `root`
- **Password:** `root` (as per `application.properties`)

## Main Tables

### 1. `people`
Represents individuals involved in the recruitment and organizational process (candidates, collaborators, recruiters, managers).

| Field | Type | Description |
|-------|------|-------------|
| `id` | INT (PK) | Unique identifier (Identity) |
| `profile_image` | VARCHAR | Path or link to profile image |
| `registration_number`| INT | Employee registration number |
| `name` | VARCHAR | Full name |
| `phone_number` | VARCHAR | Contact phone |
| `cpf` | VARCHAR | Brazilian tax ID |
| `rg` | VARCHAR | General registry number |
| `date_birth` | DATETIME | Birth date |
| `sex` | VARCHAR | Gender |
| `replacement` | BOOLEAN | Indicates if it's a replacement |
| `recruitment_data` | DATETIME | Date of recruitment data collection |
| `admission_date` | DATETIME | Date of admission |
| `observations` | TEXT | General observations |
| `professional_references` | TEXT | Professional references details |
| `collaborator_knowledge` | TEXT | Skills and knowledge |
| `labor_lawsuit` | TEXT | Labor lawsuit information |
| `criminal_background` | BOOLEAN | Criminal background check status |
| `external_link` | VARCHAR | External profile link (e.g., LinkedIn) |
| `mindsight_link` | VARCHAR | Mindsight assessment link |
| `cis_link` | VARCHAR | CIS assessment link |
| `id_resignation_motivation` | INT (FK) | Links to `resignation_motivation` |
| `id_resignation_type` | INT (FK) | Links to `resignation_type` |
| `id_education` | INT (FK) | Links to `education` |
| `id_situation` | INT (FK) | Links to `situation` |
| `id_recruitment_source` | INT (FK) | Links to `recruitment_source` |

---

### 2. `opportunities`
Represents job openings and the recruitment lifecycle.

| Field | Type | Description |
|-------|------|-------------|
| `id` | INT (PK) | Unique identifier (Identity) |
| `open_opportunity_date` | DATETIME | When the opportunity was opened |
| `people_id` | INT (FK) | The **candidate** (links to `people`) |
| `position_id` | INT (FK) | Links to `position` |
| `team_id` | INT (FK) | Links to `team` |
| `departament_id` | INT (FK) | Links to `departament` |
| `opportunity_motive_id` | INT (FK) | Links to `opportunity_motive` |
| `replaced_person_id` | INT (FK) | The **person being replaced** (links to `people`) |
| `base_origin_id` | INT (FK) | Links to `base_origin` |
| `opportunity_status_id` | INT (FK) | Links to `opportunity_status` |
| `process_stage_id` | INT (FK) | Links to `process_stage` |
| `process_status_id` | INT (FK) | Links to `process_status` |
| `deadline_sla_days` | INT | SLA in days |
| `accept_date` | DATETIME | When the offer was accepted |
| `responsible_recruiter_id` | INT (FK) | The **recruiter** (links to `people`) |
| `observations` | VARCHAR | Specific opportunity notes |


---

### 3. `recruitment_process`
Tracks the individual recruitment process of a candidate for a specific opportunity.

| Field | Type | Description |
|-------|------|-------------|
| `id` | INT (PK) | Unique identifier (Identity) |
| `people_id` | INT (FK) | Links to `people` |
| `opportunity_id` | INT (FK) | Links to `opportunities` |
| `process_status_id` | INT (FK) | Links to `process_status` |
| `process_stage_id` | INT (FK) | Links to `process_stage` |
| `recruitment_source_id` | INT (FK) | Links to `recruitment_source` |
| `situation_id` | INT (FK) | Links to `situation` |


### 4. `documents`
Stores documents uploaded for candidates.

| Field | Type | Description |
|-------|------|-------------|
| `id` | INT (PK) | Unique identifier (Identity) |
| `file_name` | VARCHAR | Original file name |
| `file_type` | VARCHAR | MIME type of the file |
| `file_path` | VARCHAR | Physical path where the file is stored |
| `people_id` | INT (FK) | Candidate ID (links to `people`) |
| `document_type_id` | INT (FK) | Document type ID (links to `document_type`) |
| `upload_date` | DATETIME | When the document was uploaded |

---

## Auxiliary Tables
Most auxiliary tables follow a standard structure: `id`, `name`, and `description`.

| Table Name | Entity Class | Fields (Specifics) |
|------------|--------------|--------------------|
| `opportunity_status` | `DhoOpportunityStatus` | `id`, `opportunity_status_name`, `opportunity_status_description` |
| `process_stage` | `DhoProcessStage` | `id`, `process_stage_name`, `process_stage_description` |
| `education` | `DhoEducation` | `id`, `education_name`, `education_description` |
| `position` | `DhoPosition` | `id`, `position_name`, `position_description` |
| `base_origin` | `DhoBaseOrigin` | `id`, `base_origin_name`, `base_origin_description` |
| `resignation_type` | `DhoResignationType` | `id`, `resignation_type_name`, `resignation_type_description` |
| `situation` | `DhoSituation` | `id`, `situation_name`, `situation_description` |
| `resignation_motivation`| `DhoResignationMotivation`| `id`, `resignation_motivation_name`, `resignation_motivation_description` |
| `recruitment_source`| `DhoRecruitmentSource`| `id`, `recruitment_source_name`, `recruitment_source_description` |
| `departament` | `DhoDepartment` | `id`, `departament_name`, `departament_description` |
| `opportunity_motive`| `DhoOpportunityMotive`| `id`, `opportunity_motive_name`, `opportunity_motive_description` |
| `team` | `DhoTeam` | `id`, `team_name`, `team_description`, `manager_id` (FK to `people`) |
| `process_status` | `DhoProcessStatus` | `id`, `process_status_name`, `process_status_description` |
| `document_type` | `DhoDocumentType` | `id`, `document_type_name`, `document_type_description` |


---

## Entity Relationships

1.  **Multiple Roles for `people`**:
    *   In `opportunities`: One `people` record can be a `candidate`, another can be a `replacedPerson`, and another can be the `responsibleRecruiter`.
    *   In `team`: One `people` record can be the `manager`.
2.  **Standard Lookups**:
 3.  **Recruitment Process**:
     *   `recruitment_process` acts as a link between `people` and `opportunities`, tracking the candidate's journey through various stages and statuses.

    *   `people` links to `resignation_motivation`, `resignation_type`, `education`, `situation`, and `recruitment_source`.
    *   `opportunities` links to `position`, `team`, `departament`, `opportunity_motive`, `base_origin`, `opportunity_status`, `process_stage`, and `process_status`.

## Implementation Notes
- **Naming Conventions**: There is some inconsistency in foreign key naming (e.g., `people_id` vs `id_education`).
- **Data Types**: Dates are handled using `java.time.LocalDateTime`.
- **Lombok**: The project uses Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) for boilerplate reduction.
- **Persistence**: Managed by Jakarta Persistence (JPA).
