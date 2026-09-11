package assembly.general.api.dto.request;

import assembly.general.api.enums.BookCondition;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReturnRequest {

    @NotNull(message = "condition is required (GOOD, FAIR, POOR, or DAMAGED)")
    private BookCondition condition;

    // Optional
    private String notes;
}
