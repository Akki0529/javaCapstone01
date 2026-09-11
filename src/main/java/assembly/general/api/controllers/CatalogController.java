package assembly.general.api.controllers;

import assembly.general.api.dto.response.BookDetailResponse;
import assembly.general.api.dto.response.BookSummaryResponse;
import assembly.general.api.dto.response.PagedResponse;
import assembly.general.api.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catalog", description = "Public book catalog endpoints — no authentication required")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * GET /api/catalog/books — public
     * Does the  pagination, sorting, full-text search (query), genre, isbn, availableOnly filters.
     */
    @GetMapping("/books")
    @Operation(summary = "Browse and search the book catalog with pagination")
    public ResponseEntity<PagedResponse<BookSummaryResponse>> getBooks(
            @Parameter(description = "Zero-based page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field: title, author, publicationYear") @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction: asc, desc") @RequestParam(defaultValue = "asc") String sortOrder,
            @Parameter(description = "Search term (title or author)") @RequestParam(required = false) String query,
            @Parameter(description = "Filter by genre (exact match)") @RequestParam(required = false) String genre,
            @Parameter(description = "Filter by ISBN (exact match)") @RequestParam(required = false) String isbn,
            @Parameter(description = "Show only books with available copies") @RequestParam(defaultValue = "false") Boolean availableOnly) {

        PagedResponse<BookSummaryResponse> result =
            catalogService.getBooks(page, size, sortBy, sortOrder, query, genre, isbn, availableOnly);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/catalog/books/{bookId} — public
     */
    @GetMapping("/books/{bookId}")
    @Operation(summary = "View detailed information for a specific book")
    public ResponseEntity<BookDetailResponse> getBook(
            @Parameter(description = "Book UUID") @PathVariable UUID bookId) {
        return ResponseEntity.ok(catalogService.getBookById(bookId));
    }
}
