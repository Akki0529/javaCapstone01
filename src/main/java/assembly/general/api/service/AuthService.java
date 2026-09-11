package assembly.general.api.service;

import assembly.general.api.dto.request.LoginRequest;
import assembly.general.api.dto.request.RegisterRequest;
import assembly.general.api.dto.response.LoginResponse;
import assembly.general.api.dto.response.RegisterResponse;
import assembly.general.api.entity.User;
import assembly.general.api.enums.MembershipStatus;
import assembly.general.api.enums.Role;
import assembly.general.api.exception.EmailAlreadyExistsException;
import assembly.general.api.exception.InvalidCredentialsException;
import assembly.general.api.repository.UserRepository;
import assembly.general.api.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Register a new user.  Assigns PATRONrole and ACTIVE status by default.
     * throws the  EmailAlreadyExistsException if the email si already taken
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(Role.PATRON);
        user.setMembershipStatus(MembershipStatus.ACTIVE);
        user.setMemberSince(Instant.now());

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getEmail());

        return RegisterResponse.builder()
            .userId(saved.getId())
            .email(saved.getEmail())
            .firstName(saved.getFirstName())
            .lastName(saved.getLastName())
            .role(saved.getRole())
            .membershipStatus(saved.getMembershipStatus())
            .createdAt(saved.getCreatedAt())
            .message("Registration successful")
            .build();
    }

    /**
     * Authenticate a user. Returns a JWT token on success.
     * Throws InvalidCredentialsException on failure (email not found or wrong password).
     * Does not reveal which if a check is failed as per the spec
     *
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

        return LoginResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(tokenProvider.getExpirationSeconds())
            .user(LoginResponse.UserInfo.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build())
            .build();
    }
}
