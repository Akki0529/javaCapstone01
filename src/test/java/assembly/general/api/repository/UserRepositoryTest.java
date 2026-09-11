package assembly.general.api.repository;

import assembly.general.api.config.JpaConfig;
import assembly.general.api.entity.User;
import assembly.general.api.enums.MembershipStatus;
import assembly.general.api.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// @Import(JpaConfig.class) loads @EnableJpaAuditing so @CreatedDate / @LastModifiedDate
// are populated — @DataJpaTest is a slice that excludes @Configuration beans by default.
@DataJpaTest
@Import(JpaConfig.class)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    private User makeUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPassword("hashed");
        u.setFirstName("Test");
        u.setLastName("User");
        u.setPhoneNumber("+1-555-0000");
        u.setRole(Role.PATRON);
        u.setMembershipStatus(MembershipStatus.ACTIVE);
        u.setMemberSince(Instant.now());
        return u;
    }

    @Test
    void findByEmail_returnsUser_whenExists() {
        em.persistAndFlush(makeUser("test@example.com"));

        Optional<User> result = userRepository.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_returnsEmpty_whenNotFound() {
        Optional<User> result = userRepository.findByEmail("ghost@example.com");
        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_returnsTrue_whenEmailExists() {
        em.persistAndFlush(makeUser("exists@example.com"));
        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
    }

    @Test
    void existsByEmail_returnsFalse_whenEmailMissing() {
        assertThat(userRepository.existsByEmail("none@example.com")).isFalse();
    }

    @Test
    void email_uniqueConstraint_enforced() {
        em.persistAndFlush(makeUser("dup@example.com"));
        User dup = makeUser("dup@example.com");

        assertThatThrownBy(() -> {
            em.persistAndFlush(dup);
        }).isInstanceOf(Exception.class); // JPA / constraint violation
    }
}
