package assembly.general.api.repository;

import assembly.general.api.config.JpaConfig;
import assembly.general.api.entity.Book;
import assembly.general.api.entity.Reservation;
import assembly.general.api.entity.User;
import assembly.general.api.enums.MembershipStatus;
import assembly.general.api.enums.ReservationStatus;
import assembly.general.api.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @Import(JpaConfig.class) loads @EnableJpaAuditing so @CreatedDate / @LastModifiedDate
// are populated — @DataJpaTest is a slice that excludes @Configuration beans by default.
@DataJpaTest
@Import(JpaConfig.class)
class ReservationRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ReservationRepository reservationRepository;

    private User user;
    private Book book;

    @BeforeEach
    void setup() {
        user = new User();
        user.setEmail("patron@test.com");
        user.setPassword("hash");
        user.setFirstName("Pat");
        user.setLastName("Ron");
        user.setPhoneNumber("+1-555-0000");
        user.setRole(Role.PATRON);
        user.setMembershipStatus(MembershipStatus.ACTIVE);
        user.setMemberSince(Instant.now());
        em.persistAndFlush(user);

        book = new Book();
        book.setIsbn("978-test");
        book.setTitle("Test Book");
        book.setAuthor("Author");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        em.persistAndFlush(book);
    }

    @Test
    void countByUserIdAndStatusIn_countsActiveReservations() {
        persistReservation(ReservationStatus.RESERVED);
        persistReservation(ReservationStatus.CHECKED_OUT);
        persistReservation(ReservationStatus.RETURNED);

        long count = reservationRepository.countByUserIdAndStatusIn(
            user.getId(),
            List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT)
        );

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void countByUserIdAndStatusIn_atLimit_returns5() {
        for (int i = 0; i < 5; i++) persistReservation(ReservationStatus.RESERVED);

        long count = reservationRepository.countByUserIdAndStatusIn(
            user.getId(),
            List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT)
        );

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void findActiveByUserId_returnsOnlyActiveStatuses() {
        persistReservation(ReservationStatus.RESERVED);
        persistReservation(ReservationStatus.CHECKED_OUT);
        persistReservation(ReservationStatus.RETURNED);  // should NOT be included
        persistReservation(ReservationStatus.CANCELLED); // should NOT be included

        List<Reservation> active = reservationRepository.findActiveByUserId(
            user.getId(),
            List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT)
        );

        assertThat(active).hasSize(2);
        assertThat(active).extracting(Reservation::getStatus)
            .containsExactlyInAnyOrder(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT);
    }

    @Test
    void findHistoryByUserId_paginatesAllStatuses() {
        persistReservation(ReservationStatus.RESERVED);
        persistReservation(ReservationStatus.RETURNED);
        persistReservation(ReservationStatus.CANCELLED);

        Page<Reservation> page = reservationRepository.findHistoryByUserId(
            user.getId(), PageRequest.of(0, 20)
        );

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    void findHistoryByUserId_paginationWorks() {
        for (int i = 0; i < 5; i++) persistReservation(ReservationStatus.RETURNED);

        Page<Reservation> page = reservationRepository.findHistoryByUserId(
            user.getId(), PageRequest.of(0, 2)
        );

        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    void findActiveByUserId_joinFetchesBook() {
        persistReservation(ReservationStatus.RESERVED);

        List<Reservation> active = reservationRepository.findActiveByUserId(
            user.getId(),
            List.of(ReservationStatus.RESERVED)
        );

        assertThat(active).hasSize(1);
        // Accessing book should NOT cause LazyInitializationException (JOIN FETCH)
        assertThat(active.get(0).getBook().getTitle()).isEqualTo("Test Book");
    }

    // ---

    private Reservation persistReservation(ReservationStatus status) {
        Instant now = Instant.now();
        Reservation r = new Reservation();
        r.setUser(user);
        r.setBook(book);
        r.setStatus(status);
        r.setReservedAt(now);
        r.setExpiresAt(now.plus(7, ChronoUnit.DAYS));
        if (status == ReservationStatus.CHECKED_OUT || status == ReservationStatus.RETURNED) {
            r.setCheckedOutAt(now.plus(1, ChronoUnit.DAYS));
            r.setDueDate(now.plus(15, ChronoUnit.DAYS));
        }
        if (status == ReservationStatus.RETURNED) {
            r.setReturnedAt(now.plus(10, ChronoUnit.DAYS));
        }
        em.persistAndFlush(r);
        return r;
    }
}
