package assembly.general.api.controllers;

import assembly.general.api.dto.request.LoginRequest;
import assembly.general.api.dto.request.RegisterRequest;
import assembly.general.api.dto.response.LoginResponse;
import assembly.general.api.dto.response.RegisterResponse;
import assembly.general.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/register — public */
    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** POST /api/auth/login — public */
    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
