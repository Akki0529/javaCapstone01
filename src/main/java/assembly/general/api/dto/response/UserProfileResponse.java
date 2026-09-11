package assembly.general.api.dto.response;

import assembly.general.api.enums.MembershipStatus;
import assembly.general.api.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class UserProfileResponse {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Role role;
    private MembershipStatus membershipStatus;
    private Instant memberSince;
    private long activeReservations;
    private long borrowingHistory;
}
