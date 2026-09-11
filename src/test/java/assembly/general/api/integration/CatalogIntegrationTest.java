package assembly.general.api.integration;

import assembly.general.api.entity.Book;
import assembly.general.api.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    private UUID seedBookId;

    @BeforeEach
    void seed() {
        // DataInitializer runs on context load; if books already exist from DataInitializer that's fine.
        // We seed an additional known book for precise assertions.
        Book b = new Book();
        b.setIsbn("test-integration-isbn");
        b.setTitle("Integration Test Book");
        b.setAuthor("Test Author");
        b.setGenre("Testing");
        b.setPublicationYear(2024);
        b.setDescription("A book for integration tests");
        b.setPublisher("Test Press");
        b.setPageCount(100);
        b.setLanguage("English");
        b.setTotalCopies(3);
        b.setAvailableCopies(3);
        Book saved = bookRepository.save(b);
        seedBookId = saved.getId();
    }

    @Test
    void getBooks_defaultPagination_returns200() throws Exception {
        mockMvc.perform(get("/api/catalog/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").isNumber())
            .andExpect(jsonPath("$.totalPages").isNumber())
            .andExpect(jsonPath("$.last").isBoolean());
    }

    @Test
    void getBooks_doesNotRequireAuthentication() throws Exception {
        // no Authorization header
        mockMvc.perform(get("/api/catalog/books"))
            .andExpect(status().isOk());
    }

    @Test
    void getBooks_searchByTitle_returnsMatches() throws Exception {
        mockMvc.perform(get("/api/catalog/books").param("query", "Integration Test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.title == 'Integration Test Book')]").exists());
    }

    @Test
    void getBooks_searchByAuthor_returnsMatches() throws Exception {
        mockMvc.perform(get("/api/catalog/books").param("query", "Test Author"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.author == 'Test Author')]").exists());
    }

    @Test
    void getBooks_filterByGenre() throws Exception {
        mockMvc.perform(get("/api/catalog/books").param("genre", "Testing"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].genre").value("Testing"));
    }

    @Test
    void getBooks_filterByIsbn() throws Exception {
        mockMvc.perform(get("/api/catalog/books").param("isbn", "test-integration-isbn"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].isbn").value("test-integration-isbn"));
    }

    @Test
    void getBooks_availableOnly_excludesZeroCopies() throws Exception {
        // Create a book with 0 copies
        Book out = new Book();
        out.setIsbn("test-unavailable");
        out.setTitle("Unavailable Book");
        out.setAuthor("Nobody");
        out.setTotalCopies(1);
        out.setAvailableCopies(0);
        bookRepository.save(out);

        mockMvc.perform(get("/api/catalog/books")
                .param("availableOnly", "true")
                .param("isbn", "test-unavailable"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getBooks_emptySearch_returnsEmptyContent() throws Exception {
        mockMvc.perform(get("/api/catalog/books").param("query", "xyzzy-definitely-not-found"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getBook_byId_returnsFullDetail() throws Exception {
        mockMvc.perform(get("/api/catalog/books/" + seedBookId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bookId").value(seedBookId.toString()))
            .andExpect(jsonPath("$.title").value("Integration Test Book"))
            .andExpect(jsonPath("$.isbn").value("test-integration-isbn"))
            .andExpect(jsonPath("$.publisher").value("Test Press"))
            .andExpect(jsonPath("$.pageCount").value(100))
            .andExpect(jsonPath("$.language").value("English"))
            .andExpect(jsonPath("$.status").value("AVAILABLE"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty())
            .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void getBook_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/catalog/books/" + UUID.randomUUID()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void bookStatus_computedDynamically() throws Exception {
        // seedBookId has availableCopies=3 → AVAILABLE
        mockMvc.perform(get("/api/catalog/books/" + seedBookId))
            .andExpect(jsonPath("$.status").value("AVAILABLE"));

        // Set copies to 0 and check
        Book b = bookRepository.findById(seedBookId).orElseThrow();
        b.setAvailableCopies(0);
        bookRepository.save(b);

        mockMvc.perform(get("/api/catalog/books/" + seedBookId))
            .andExpect(jsonPath("$.status").value("CHECKED_OUT"));
    }
}
