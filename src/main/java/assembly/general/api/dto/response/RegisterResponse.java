package assembly.general.api.dto.response;

import assembly.general.api.enums.MembershipStatus;
import assembly.general.api.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class RegisterResponse {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private MembershipStatus membershipStatus;
    private Instant createdAt;
    private String message;
}
