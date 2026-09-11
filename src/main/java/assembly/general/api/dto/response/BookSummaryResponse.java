package assembly.general.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/** Used in the paginated catalog list (GET /api/catalog/books). */
@Getter
@Builder
public class BookSummaryResponse {
    private UUID bookId;
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private Integer publicationYear;
    private String description;
    private Integer totalCopies;
    private Integer availableCopies;
    private String status; // "AVAILABLE" or "CHECKED_OUT" — computed, not stored
}
