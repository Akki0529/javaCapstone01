package assembly.general.api.repository;

import assembly.general.api.entity.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds JPA Specifications for dynamic catalog filtering.
 * Supports: full-text search on title/author, genre, ISBN, and availability.
 */
public class BookSpecification {

    private BookSpecification() {}

    public static Specification<Book> withFilters(
            String query,
            String genre,
            String isbn,
            Boolean availableOnly) {

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate authorMatch = cb.like(cb.lower(root.get("author")), pattern);
                predicates.add(cb.or(titleMatch, authorMatch));
            }

            if (genre != null && !genre.isBlank()) {
                predicates.add(cb.equal(root.get("genre"), genre));
            }

            if (isbn != null && !isbn.isBlank()) {
                predicates.add(cb.equal(root.get("isbn"), isbn));
            }

            if (Boolean.TRUE.equals(availableOnly)) {
                predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
