package assembly.general.api.repository;

import assembly.general.api.config.JpaConfig;
import assembly.general.api.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// @Import(JpaConfig.class) loads @EnableJpaAuditing so @CreatedDate / @LastModifiedDate
// are populated — @DataJpaTest is a slice that excludes @Configuration beans by default.
@DataJpaTest
@Import(JpaConfig.class)
class BookRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void seed() {
        em.persistAndFlush(book("978-1", "Clean Code", "Robert Martin", "Technology", 2008, 3, 3));
        em.persistAndFlush(book("978-2", "Refactoring", "Martin Fowler", "Technology", 2018, 2, 0));
        em.persistAndFlush(book("978-3", "Harry Potter", "J.K. Rowling", "Fantasy", 1997, 5, 5));
        em.persistAndFlush(book("978-4", "Clean Architecture", "Robert Martin", "Technology", 2017, 1, 1));
    }

    @Test
    void fullTextSearch_byTitle() {
        Specification<Book> spec = BookSpecification.withFilters("clean", null, null, null);
        Page<Book> page = bookRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getContent()).extracting(Book::getTitle)
            .containsExactlyInAnyOrder("Clean Code", "Clean Architecture");
    }

    @Test
    void fullTextSearch_byAuthor() {
        Specification<Book> spec = BookSpecification.withFilters("fowler", null, null, null);
        Page<Book> page = bookRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Refactoring");
    }

    @Test
    void filterByGenre() {
        Specification<Book> spec = BookSpecification.withFilters(null, "Fantasy", null, null);
        Page<Book> page = bookRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Harry Potter");
    }

    @Test
    void filterByIsbn_exactMatch() {
        Specification<Book> spec = BookSpecification.withFilters(null, null, "978-1", null);
        Page<Book> page = bookRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getIsbn()).isEqualTo("978-1");
    }

    @Test
    void filterAvailableOnly_excludesZeroCopies() {
        Specification<Book> spec = BookSpecification.withFilters(null, null, null, true);
        Page<Book> page = bookRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getContent())
            .allMatch(b -> b.getAvailableCopies() > 0);
        // Refactoring (availableCopies=0) should NOT appear
        assertThat(page.getContent()).extracting(Book::getTitle)
            .doesNotContain("Refactoring");
    }

    @Test
    void combinedFilters_queryPlusGenrePlusAvailableOnly() {
        Specification<Book> spec = BookSpecification.withFilters("clean", "Technology", null, true);
        Page<Book> page = bookRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getContent())
            .allMatch(b -> b.getAvailableCopies() > 0)
            .allMatch(b -> "Technology".equals(b.getGenre()));
    }

    @Test
    void sortByTitle_ascending() {
        Page<Book> page = bookRepository.findAll(
            Specification.where(null),
            PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "title"))
        );

        assertThat(page.getContent()).extracting(Book::getTitle)
            .isSortedAccordingTo(String::compareToIgnoreCase);
    }

    @Test
    void paginationMetadata_isCorrect() {
        Page<Book> page = bookRepository.findAll(
            Specification.where(null),
            PageRequest.of(0, 2)
        );

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.isLast()).isFalse();
    }

    @Test
    void emptySearch_returnsEmptyPage_notError() {
        Specification<Book> spec = BookSpecification.withFilters("xyzzy-not-found", null, null, null);
        Page<Book> page = bookRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void isbn_uniqueConstraint_enforced() {
        assertThatThrownBy(() ->
            em.persistAndFlush(book("978-1", "Duplicate", "Author", "Tech", 2000, 1, 1))
        ).isInstanceOf(Exception.class);
    }

    // ---

    private Book book(String isbn, String title, String author, String genre,
                      int year, int total, int available) {
        Book b = new Book();
        b.setIsbn(isbn);
        b.setTitle(title);
        b.setAuthor(author);
        b.setGenre(genre);
        b.setPublicationYear(year);
        b.setTotalCopies(total);
        b.setAvailableCopies(available);
        return b;
    }
}
