package assembly.general.api.service;

import assembly.general.api.dto.response.BookDetailResponse;
import assembly.general.api.dto.response.BookSummaryResponse;
import assembly.general.api.dto.response.PagedResponse;
import assembly.general.api.entity.Book;
import assembly.general.api.exception.BookNotFoundException;
import assembly.general.api.repository.BookRepository;
import assembly.general.api.repository.BookSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CatalogService {

    private static final java.util.Set<String> SORTABLE_FIELDS =
            java.util.Set.of("title", "author", "publicationYear");

    private final BookRepository bookRepository;

    public CatalogService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Return a paginated, filtered, and sorted list of books.
     * Matches GET /api/catalog/books query parameters exactly.
     */
    @Transactional(readOnly = true)
    public PagedResponse<BookSummaryResponse> getBooks(
            int page,
            int size,
            String sortBy,
            String sortOrder,
            String query,
            String genre,
            String isbn,
            Boolean availableOnly) {

        // Guard against invalid sort fields — default to title
        String field = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "title";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, field));
        Specification<Book> spec = BookSpecification.withFilters(query, genre, isbn, availableOnly);
        Page<Book> result = bookRepository.findAll(spec, pageable);

        return PagedResponse.of(result, this::toSummary);
    }

    /**
     * Return full detail for a single book.
     * Matches GET /api/catalog/books/{bookId}.
     */
    @Transactional(readOnly = true)
    public BookDetailResponse getBookById(UUID bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));
        return toDetail(book);
    }

    // --- Mappers ---

    private BookSummaryResponse toSummary(Book book) {
        return BookSummaryResponse.builder()
            .bookId(book.getId())
            .isbn(book.getIsbn())
            .title(book.getTitle())
            .author(book.getAuthor())
            .genre(book.getGenre())
            .publicationYear(book.getPublicationYear())
            .description(book.getDescription())
            .totalCopies(book.getTotalCopies())
            .availableCopies(book.getAvailableCopies())
            .status(computeStatus(book))
            .build();
    }

    private BookDetailResponse toDetail(Book book) {
        return BookDetailResponse.builder()
            .bookId(book.getId())
            .isbn(book.getIsbn())
            .title(book.getTitle())
            .author(book.getAuthor())
            .genre(book.getGenre())
            .publicationYear(book.getPublicationYear())
            .description(book.getDescription())
            .publisher(book.getPublisher())
            .pageCount(book.getPageCount())
            .language(book.getLanguage())
            .totalCopies(book.getTotalCopies())
            .availableCopies(book.getAvailableCopies())
            .status(computeStatus(book))
            .createdAt(book.getCreatedAt())
            .updatedAt(book.getUpdatedAt())
            .build();
    }

    /** Dynamically determine status from availableCopies — NOT stored in the database. */
    private String computeStatus(Book book) {
        return book.getAvailableCopies() > 0 ? "AVAILABLE" : "CHECKED_OUT";
    }
}
