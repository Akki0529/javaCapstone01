package assembly.general.api.controllers;

import assembly.general.api.dto.request.CheckoutRequest;
import assembly.general.api.dto.request.ReturnRequest;
import assembly.general.api.dto.request.ReservationRequest;
import assembly.general.api.dto.response.*;
import assembly.general.api.security.UserPrincipal;
import assembly.general.api.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation lifecycle: reserve, checkout, return, history")
@SecurityRequirement(name = "BearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * POST /api/reservations — requires authentication (any role)
     * Creates a reservation. Enforces 5-reservation limit and book availability.
     */
    @PostMapping
    @Operation(summary = "Reserve an available book")
    public ResponseEntity<ReservationCreateResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        ReservationCreateResponse response =
            reservationService.createReservation(request.getBookId(), principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/reservations — requires authentication
     * Returns all active (RESERVED or CHECKED_OUT) reservations for the current user.
     */
    @GetMapping
    @Operation(summary = "View active reservations for the current user")
    public ResponseEntity<ActiveReservationsResponse> getActiveReservations(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(reservationService.getActiveReservations(principal));
    }

    /**
     * POST /api/reservations/{reservationId}/checkout — LIBRARIAN only
     * Transitions reservation from RESERVED → CHECKED_OUT, sets 14-day due date.
     */
    @PostMapping("/{reservationId}/checkout")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Process book checkout (LIBRARIAN only)")
    public ResponseEntity<CheckoutResponse> checkout(
            @PathVariable UUID reservationId,
            @RequestBody(required = false) CheckoutRequest request) {

        return ResponseEntity.ok(reservationService.checkout(reservationId, request));
    }

    /**
     * POST /api/reservations/{reservationId}/return — LIBRARIAN only
     * Transitions reservation from CHECKED_OUT → RETURNED. Calculates late fees.
     */
    @PostMapping("/{reservationId}/return")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Process book return with late fee calculation (LIBRARIAN only)")
    public ResponseEntity<ReturnResponse> returnBook(
            @PathVariable UUID reservationId,
            @Valid @RequestBody ReturnRequest request) {

        return ResponseEntity.ok(reservationService.returnBook(reservationId, request));
    }

    /**
     * GET /api/reservations/history — requires authentication
     * Returns paginated borrowing history for the current user (all statuses).
     */
    @GetMapping("/history")
    @Operation(summary = "View complete borrowing history with pagination")
    public ResponseEntity<PagedResponse<HistoryItemResponse>> getBorrowingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(reservationService.getBorrowingHistory(principal, page, size));
    }
}
