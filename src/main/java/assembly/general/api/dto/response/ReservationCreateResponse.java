package assembly.general.api.dto.response;

import assembly.general.api.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** Response for POST /api/reservations (201 Created). */
@Getter
@Builder
public class ReservationCreateResponse {
    private UUID reservationId;
    private UUID bookId;
    private UUID userId;
    private String bookTitle;
    private ReservationStatus status;
    private Instant reservedAt;
    private Instant expiresAt;
    private String message;
}
