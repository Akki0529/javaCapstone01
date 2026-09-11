package assembly.general.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid format")
    private String email;

    /**
     * Min 8 chars, at least one uppercase, one lowercase, one digit, one special character.
     * Per US-001 acceptance criteria.
     */
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_#^()\\-+=<>{}\\[\\]|~`])[A-Za-z\\d@$!%*?&_#^()\\-+=<>{}\\[\\]|~`]{8,}$",
        message = "Password must be at least 8 characters and include uppercase, lowercase, number, and special character"
    )
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^[+]?[0-9\\s\\-().]{7,20}$",
        message = "Phone number must be a valid format"
    )
    private String phoneNumber;
}
