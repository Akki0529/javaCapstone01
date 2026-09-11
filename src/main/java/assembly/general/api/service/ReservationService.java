package assembly.general.api.service;

import assembly.general.api.dto.request.CheckoutRequest;
import assembly.general.api.dto.request.ReturnRequest;
import assembly.general.api.dto.response.*;
import assembly.general.api.entity.Book;
import assembly.general.api.entity.Reservation;
import assembly.general.api.enums.ReservationStatus;
import assembly.general.api.exception.BookNotFoundException;
import assembly.general.api.exception.BookUnavailableException;
import assembly.general.api.exception.InvalidStatusException;
import assembly.general.api.exception.ReservationLimitExceededException;
import assembly.general.api.exception.ReservationNotFoundException;
import assembly.general.api.repository.BookRepository;
import assembly.general.api.repository.ReservationRepository;
import assembly.general.api.repository.UserRepository;
import assembly.general.api.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ReservationService {

    private static final int MAX_ACTIVE_RESERVATIONS = 5;
    private static final int RESERVATION_EXPIRY_DAYS = 7;
    private static final int CHECKOUT_PERIOD_DAYS = 14;
    private static final BigDecimal LATE_FEE_PER_DAY = BigDecimal.ONE;

    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT);

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              BookRepository bookRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a reservation for the authenticated user.
     * Enforces: 5-reservation limit, book must have available copies.
     * Decrements availableCopies by 1.
     */
    @Transactional
    public ReservationCreateResponse createReservation(UUID bookId, UserPrincipal principal) {
        UUID userId = principal.getId();

        // Check active reservation limit
        long activeCount = reservationRepository.countByUserIdAndStatusIn(userId, ACTIVE_STATUSES);
        if (activeCount >= MAX_ACTIVE_RESERVATIONS) {
            throw new ReservationLimitExceededException(activeCount);
        }

        // Check book exists and has available copies
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new BookUnavailableException();
        }

        // Decrement available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Create reservation
        Instant now = Instant.now();
        Reservation reservation = new Reservation();
        reservation.setBook(book);

        // getReferenceById returns a proxy without hitting the DB — sufficient for FK persistence
        reservation.setUser(userRepository.getReferenceById(userId));

        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setReservedAt(now);
        reservation.setExpiresAt(now.plus(RESERVATION_EXPIRY_DAYS, ChronoUnit.DAYS));

        Reservation saved = reservationRepository.save(reservation);

        return ReservationCreateResponse.builder()
            .reservationId(saved.getId())
            .bookId(book.getId())
            .userId(userId)
            .bookTitle(book.getTitle())
            .status(saved.getStatus())
            .reservedAt(saved.getReservedAt())
            .expiresAt(saved.getExpiresAt())
            .message("Book reserved successfully. Please pick up within 7 days.")
            .build();
    }

    /**
     * Return all active reservations (RESERVED or CHECKED_OUT) for the authenticated user.
     * Calculates daysUntilExpiry (for RESERVED) and daysUntilDue (for CHECKED_OUT).
     */
    @Transactional(readOnly = true)
    public ActiveReservationsResponse getActiveReservations(UserPrincipal principal) {
        List<Reservation> reservations =
            reservationRepository.findActiveByUserId(principal.getId(), ACTIVE_STATUSES);

        Instant now = Instant.now();

        List<ActiveReservationItem> items = reservations.stream()
            .map(r -> {
                ActiveReservationItem.ActiveReservationItemBuilder builder = ActiveReservationItem.builder()
                    .reservationId(r.getId())
                    .bookId(r.getBook().getId())
                    .bookTitle(r.getBook().getTitle())
                    .bookAuthor(r.getBook().getAuthor())
                    .status(r.getStatus())
                    .reservedAt(r.getReservedAt());

                if (r.getStatus() == ReservationStatus.RESERVED) {
                    builder
                        .expiresAt(r.getExpiresAt())
                        .daysUntilExpiry(Math.max(0, ChronoUnit.DAYS.between(now, r.getExpiresAt())));
                } else if (r.getStatus() == ReservationStatus.CHECKED_OUT) {
                    builder
                        .checkedOutAt(r.getCheckedOutAt())
                        .dueDate(r.getDueDate())
                        .daysUntilDue(Math.max(0, ChronoUnit.DAYS.between(now, r.getDueDate())));
                }

                return builder.build();
            })
            .toList();

        return ActiveReservationsResponse.builder()
            .reservations(items)
            .totalActive(items.size())
            .build();
    }

    /**
     * Process a checkout (LIBRARIAN only — enforced at controller layer via @PreAuthorize).
     * Transition: RESERVED → CHECKED_OUT.
     * Sets checkedOutAt and dueDate (14 days from checkout).
     */
    @Transactional
    public CheckoutResponse checkout(UUID reservationId, CheckoutRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new InvalidStatusException(
                "Can only checkout reservations with RESERVED status",
                reservation.getStatus()
            );
        }

        Instant now = Instant.now();
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservation.setCheckedOutAt(now);
        reservation.setDueDate(now.plus(CHECKOUT_PERIOD_DAYS, ChronoUnit.DAYS));

        if (request != null && request.getNotes() != null) {
            reservation.setNotes(request.getNotes());
        }

        Reservation saved = reservationRepository.save(reservation);

        // Format due date for the message: "October 13, 2025"
        String formattedDue = DateTimeFormatter
            .ofPattern("MMMM d, yyyy", Locale.ENGLISH)
            .withZone(java.time.ZoneOffset.UTC)
            .format(saved.getDueDate());

        return CheckoutResponse.builder()
            .reservationId(saved.getId())
            .status(saved.getStatus())
            .checkedOutAt(saved.getCheckedOutAt())
            .dueDate(saved.getDueDate())
            .message("Book checked out successfully. Due date: " + formattedDue)
            .build();
    }

    /**
     * Process a return (LIBRARIAN only — enforced at controller layer via @PreAuthorize).
     * Transition: CHECKED_OUT → RETURNED.
     * Calculates late fees at $1.00/day. Increments availableCopies by 1.
     */
    @Transactional
    public ReturnResponse returnBook(UUID reservationId, ReturnRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() != ReservationStatus.CHECKED_OUT) {
            throw new InvalidStatusException(
                "Can only return books with CHECKED_OUT status",
                reservation.getStatus()
            );
        }

        Instant now = Instant.now();
        reservation.setStatus(ReservationStatus.RETURNED);
        reservation.setReturnedAt(now);
        reservation.setCondition(request.getCondition());

        if (request.getNotes() != null) {
            reservation.setNotes(request.getNotes());
        }

        // Calculate late fees
        int lateDays = 0;
        BigDecimal lateFee = BigDecimal.ZERO;

        if (reservation.getDueDate() != null && now.isAfter(reservation.getDueDate())) {
            lateDays = (int) ChronoUnit.DAYS.between(reservation.getDueDate(), now);
            lateFee = LATE_FEE_PER_DAY.multiply(BigDecimal.valueOf(lateDays));
        }

        reservation.setLateDays(lateDays);
        reservation.setLateFee(lateFee);
        reservationRepository.save(reservation);

        // Increment available copies
        Book book = reservation.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        String message = lateDays > 0
            ? String.format("Book returned. Late fee of $%.2f applied to account.", lateFee)
            : "Book returned successfully";

        return ReturnResponse.builder()
            .reservationId(reservation.getId())
            .returnedAt(now)
            .dueDate(lateDays > 0 ? reservation.getDueDate() : null)
            .lateDays(lateDays)
            .lateFee(lateFee)
            .message(message)
            .build();
    }

    /**
     * Return the authenticated user's complete borrowing history, paginated.
     * Sorted by most-recent first (COALESCE(returnedAt, reservedAt) DESC).
     * Includes wasLate flag per record.
     */
    @Transactional(readOnly = true)
    public PagedResponse<HistoryItemResponse> getBorrowingHistory(UserPrincipal principal, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Reservation> history = reservationRepository.findHistoryByUserId(principal.getId(), pageable);

        return PagedResponse.of(history, r -> {
            boolean wasLate = r.getReturnedAt() != null
                && r.getDueDate() != null
                && r.getReturnedAt().isAfter(r.getDueDate());

            return HistoryItemResponse.builder()
                .reservationId(r.getId())
                .bookTitle(r.getBook().getTitle())
                .bookAuthor(r.getBook().getAuthor())
                .reservedAt(r.getReservedAt())
                .checkedOutAt(r.getCheckedOutAt())
                .returnedAt(r.getReturnedAt())
                .dueDate(r.getDueDate())
                .status(r.getStatus())
                .wasLate(wasLate)
                .build();
        });
    }
}
