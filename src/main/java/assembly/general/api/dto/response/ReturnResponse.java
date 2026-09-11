package assembly.general.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Response for POST /api/reservations/{reservationId}/return. */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReturnResponse {
    private UUID reservationId;
    private Instant returnedAt;
    private Instant dueDate;          // included when late
    private int lateDays;
    private BigDecimal lateFee;
    private String message;
}
