package assembly.general.api.dto.response;

import assembly.general.api.enums.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * One item in the active reservations list.
 * Fields daysUntilExpiry/daysUntilDue/expiresAt/dueDate/checkedOutAt
 * are nullable — only the relevant ones are populated per status.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActiveReservationItem {
    private UUID reservationId;
    private UUID bookId;
    private String bookTitle;
    private String bookAuthor;
    private ReservationStatus status;
    private Instant reservedAt;
    private Instant expiresAt;         // present when RESERVED
    private Long daysUntilExpiry;      // present when RESERVED
    private Instant checkedOutAt;      // present when CHECKED_OUT
    private Instant dueDate;           // present when CHECKED_OUT
    private Long daysUntilDue;         // present when CHECKED_OUT
}
