package assembly.general.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/** Full book detail response (GET /api/catalog/books/{bookId}). */
@Getter
@Builder
public class BookDetailResponse {
    private UUID bookId;
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private Integer publicationYear;
    private String description;
    private String publisher;
    private Integer pageCount;
    private String language;
    private Integer totalCopies;
    private Integer availableCopies;
    private String status; // "AVAILABLE" or "CHECKED_OUT"
    private Instant createdAt;
    private Instant updatedAt;
}
