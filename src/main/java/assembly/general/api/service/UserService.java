package assembly.general.api.service;

import assembly.general.api.dto.response.UserProfileResponse;
import assembly.general.api.entity.User;
import assembly.general.api.enums.ReservationStatus;
import assembly.general.api.repository.ReservationRepository;
import assembly.general.api.repository.UserRepository;
import assembly.general.api.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public UserService(UserRepository userRepository, ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Return the authenticated user's profile including reservation statistics.
     * activeReservations = RESERVED + CHECKED_OUT count.
     * borrowingHistory    = RETURNED count (completed borrows).
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UserPrincipal principal) {
        UUID userId = principal.getId();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        long activeReservations = reservationRepository.countByUserIdAndStatusIn(
            userId,
            List.of(ReservationStatus.RESERVED, ReservationStatus.CHECKED_OUT)
        );

        long borrowingHistory = reservationRepository.countByUserIdAndStatus(
            userId,
            ReservationStatus.RETURNED
        );

        return UserProfileResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phoneNumber(user.getPhoneNumber())
            .role(user.getRole())
            .membershipStatus(user.getMembershipStatus())
            .memberSince(user.getMemberSince())
            .activeReservations(activeReservations)
            .borrowingHistory(borrowingHistory)
            .build();
    }
}
