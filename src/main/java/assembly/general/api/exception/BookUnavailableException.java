package assembly.general.api.exception;

public class BookUnavailableException extends RuntimeException {
    private final int availableCopies;

    public BookUnavailableException() {
        super("No copies available for reservation");
        this.availableCopies = 0;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }
}
