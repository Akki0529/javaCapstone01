package assembly.general.api.dto.response;

import assembly.general.api.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** Response for POST /api/reservations/{reservationId}/checkout. */
@Getter
@Builder
public class CheckoutResponse {
    private UUID reservationId;
    private ReservationStatus status;
    private Instant checkedOutAt;
    private Instant dueDate;
    private String message;
}
