package br.com.sebratel.bff.dho.domain.repository;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class PeopleRepositoryTest {

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void shouldSaveAndFindByEmail() {
        // Given
        String email = "test@example.com";
        People person = People.builder()
                .name("Test Person")
                .email(email)
                .build();
        peopleRepository.save(person);

        // When
        Optional<People> found = peopleRepository.findByEmail(email);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
        assertThat(found.get().getName()).isEqualTo("Test Person");
    }

    @Test
    public void shouldSaveAndRetrieveRoleViaJoinTable() {
        // Given
        DhoRole role = DhoRole.builder()
                .name("ADMIN")
                .description("Administrator")
                .build();
        entityManager.persist(role);

        People person = People.builder()
                .name("Admin User")
                .email("admin@example.com")
                .roles(new HashSet<>(Collections.singletonList(role)))
                .build();
        peopleRepository.save(person);

        entityManager.flush();
        entityManager.clear();

        // When
        Optional<People> found = peopleRepository.findByEmail("admin@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getRoles()).isNotEmpty();
        assertThat(found.get().getRoles().iterator().next().getName()).isEqualTo("ADMIN");
    }
}
