package assembly.general.api.dto.response;

import assembly.general.api.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private UUID userId;
        private String email;
        private String firstName;
        private String lastName;
        private Role role;
    }
}
