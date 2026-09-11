package assembly.general.api.exception;

public class ReservationLimitExceededException extends RuntimeException {
    private final long currentReservations;

    public ReservationLimitExceededException(long currentReservations) {
        super("You have reached the maximum of 5 active reservations");
        this.currentReservations = currentReservations;
    }

    public long getCurrentReservations() {
        return currentReservations;
    }
}
