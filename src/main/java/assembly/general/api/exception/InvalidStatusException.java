package assembly.general.api.exception;

import assembly.general.api.enums.ReservationStatus;

public class InvalidStatusException extends RuntimeException {
    private final ReservationStatus currentStatus;
    private final String expectedDescription;

    public InvalidStatusException(String expectedDescription, ReservationStatus currentStatus) {
        super(expectedDescription);
        this.expectedDescription = expectedDescription;
        this.currentStatus = currentStatus;
    }

    public ReservationStatus getCurrentStatus() {
        return currentStatus;
    }
}
