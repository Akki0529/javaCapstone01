package assembly.general.api.controllers;

import assembly.general.api.dto.response.UserProfileResponse;
import assembly.general.api.security.UserPrincipal;
import assembly.general.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile endpoint")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/users/profile — requires authentication */
    @GetMapping("/profile")
    @Operation(summary = "View current user profile with reservation statistics")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getProfile(principal));
    }
}
