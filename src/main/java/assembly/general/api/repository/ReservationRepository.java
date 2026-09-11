package assembly.general.api.repository;

import assembly.general.api.entity.Reservation;
import assembly.general.api.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    /**
     * Count active reservations (RESERVED or CHECKED_OUT) for a user.
     * Used to enforce the 5-reservation limit.
     */
    long countByUserIdAndStatusIn(UUID userId, List<ReservationStatus> statuses);

    /**
     * Fetch active reservations (RESERVED or CHECKED_OUT) for a user.
     * Eagerly joins book and user to avoid N+1 queries.
     */
    @Query("SELECT r FROM Reservation r JOIN FETCH r.book WHERE r.user.id = :userId AND r.status IN :statuses")
    List<Reservation> findActiveByUserId(@Param("userId") UUID userId,
                                         @Param("statuses") List<ReservationStatus> statuses);

    /**
     * Fetch complete borrowing history for a user (all statuses), sorted most-recent first.
     * COALESCE(returnedAt, reservedAt) gives the most meaningful "recency" date.
     */
    @Query("SELECT r FROM Reservation r JOIN FETCH r.book WHERE r.user.id = :userId " +
           "ORDER BY COALESCE(r.returnedAt, r.reservedAt) DESC")
    Page<Reservation> findHistoryByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Count returned (completed) reservations for a user — used in profile borrowingHistory stat.
     */
    long countByUserIdAndStatus(UUID userId, ReservationStatus status);
}
