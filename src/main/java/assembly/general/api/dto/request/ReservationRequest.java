package assembly.general.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ReservationRequest {

    @NotNull(message = "bookId is required")
    private UUID bookId;
}
