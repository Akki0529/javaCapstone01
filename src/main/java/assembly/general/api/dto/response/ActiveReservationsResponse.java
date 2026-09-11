package assembly.general.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** Response for GET /api/reservations. */
@Getter
@Builder
public class ActiveReservationsResponse {
    private List<ActiveReservationItem> reservations;
    private int totalActive;
}
