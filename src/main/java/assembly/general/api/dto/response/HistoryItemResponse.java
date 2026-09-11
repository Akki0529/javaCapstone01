package assembly.general.api.dto.response;

import assembly.general.api.enums.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** One item in the borrowing history list (GET /api/reservations/history). */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryItemResponse {
    private UUID reservationId;
    private String bookTitle;
    private String bookAuthor;
    private Instant reservedAt;
    private Instant checkedOutAt;
    private Instant returnedAt;
    private Instant dueDate;
    private ReservationStatus status;
    private boolean wasLate;
}
