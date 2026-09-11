package assembly.general.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheckoutRequest {

    // Optional — notes about book condition at checkout
    private String notes;
}
