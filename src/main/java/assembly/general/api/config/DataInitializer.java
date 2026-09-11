package assembly.general.api.config;

import assembly.general.api.entity.Book;
import assembly.general.api.entity.User;
import assembly.general.api.enums.MembershipStatus;
import assembly.general.api.enums.Role;
import assembly.general.api.repository.BookRepository;
import assembly.general.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Seeds the in-memory H2 database with a librarian account and sample books
 * so the full reserve → checkout → return flow can be exercised immediately.
 *
 * Only runs when the DB is empty (idempotent — safe on every restart).
 *
 * Default credentials:
 *   LIBRARIAN  librarian@library.com / Librarian123!
 *   PATRON     created via POST /api/auth/register
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           BookRepository bookRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedLibrarian();
        seedBooks();
    }

    private void seedLibrarian() {
        if (userRepository.existsByEmail("librarian@library.com")) return;

        User librarian = new User();
        librarian.setEmail("librarian@library.com");
        librarian.setPassword(passwordEncoder.encode("Librarian123!"));
        librarian.setFirstName("Library");
        librarian.setLastName("Admin");
        librarian.setPhoneNumber("+1-555-0001");
        librarian.setRole(Role.LIBRARIAN);
        librarian.setMembershipStatus(MembershipStatus.ACTIVE);
        librarian.setMemberSince(Instant.now());
        userRepository.save(librarian);
        log.info("Seeded LIBRARIAN account: librarian@library.com / Librarian123!");
    }

    private void seedBooks() {
        if (bookRepository.count() > 0) return;

        List<Book> books = List.of(
            book("978-0-13-468599-1", "Clean Code",
                "Robert C. Martin", "Technology", 2008,
                "A handbook of agile software craftsmanship.",
                "Prentice Hall", 464, "English", 5, 5),
            book("978-0-13-475759-9", "Refactoring",
                "Martin Fowler", "Technology", 2018,
                "Improving the design of existing code.",
                "Addison-Wesley", 448, "English", 3, 3),
            book("978-0-20-163361-0", "The Pragmatic Programmer",
                "Andrew Hunt", "Technology", 2019,
                "Your journey to mastery.",
                "Addison-Wesley", 352, "English", 4, 4),
            book("978-0-59-651798-1", "Head First Design Patterns",
                "Eric Freeman", "Technology", 2004,
                "A brain-friendly guide to design patterns.",
                "O'Reilly Media", 688, "English", 2, 2),
            book("978-0-13-235088-4", "The Clean Coder",
                "Robert C. Martin", "Technology", 2011,
                "A code of conduct for professional programmers.",
                "Prentice Hall", 256, "English", 3, 3),
            book("978-1-49-196912-7", "Spring in Action",
                "Craig Walls", "Technology", 2022,
                "The definitive guide to Spring.",
                "Manning", 520, "English", 4, 4),
            book("978-0-06-112008-4", "To Kill a Mockingbird",
                "Harper Lee", "Fiction", 1960,
                "A novel about racial injustice and moral growth.",
                "J. B. Lippincott", 281, "English", 5, 5),
            book("978-0-74-325373-4", "The Great Gatsby",
                "F. Scott Fitzgerald", "Fiction", 1925,
                "A portrait of the Jazz Age.",
                "Charles Scribner's Sons", 180, "English", 3, 3),
            book("978-0-54-579928-0", "Harry Potter and the Philosopher's Stone",
                "J.K. Rowling", "Fantasy", 1997,
                "A young wizard's journey begins.",
                "Bloomsbury", 223, "English", 6, 6),
            book("978-0-06-093546-9", "1984",
                "George Orwell", "Fiction", 1949,
                "A dystopian social science fiction novel.",
                "Secker & Warburg", 328, "English", 4, 4)
        );

        bookRepository.saveAll(books);
        log.info("Seeded {} sample books into catalog.", books.size());
    }

    private Book book(String isbn, String title, String author, String genre,
                      int year, String description, String publisher,
                      int pages, String language, int total, int available) {
        Book b = new Book();
        b.setIsbn(isbn);
        b.setTitle(title);
        b.setAuthor(author);
        b.setGenre(genre);
        b.setPublicationYear(year);
        b.setDescription(description);
        b.setPublisher(publisher);
        b.setPageCount(pages);
        b.setLanguage(language);
        b.setTotalCopies(total);
        b.setAvailableCopies(available);
        return b;
    }
}
