package assembly.general.api.integration;

import assembly.general.api.dto.request.LoginRequest;
import assembly.general.api.dto.request.RegisterRequest;
import assembly.general.api.entity.Book;
import assembly.general.api.repository.BookRepository;
import assembly.general.api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the full reservation lifecycle: reserve → checkout → return.
 * Also tests business rules: 5-reservation limit, book availability, role restrictions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private String patronToken;
    private String librarianToken;
    private UUID bookId;

    @BeforeEach
    void setup() throws Exception {
        // Librarian was seeded by DataInitializer
        librarianToken = loginAs("librarian@library.com", "Librarian123!");

        // Create a patron
        patronToken = registerAndLogin("patron@test.com", "SecurePass123!");

        // Create a book with available copies
        Book b = new Book();
        b.setIsbn("res-test-isbn");
        b.setTitle("Reservation Test Book");
        b.setAuthor("Res Author");
        b.setTotalCopies(5);
        b.setAvailableCopies(5);
        Book saved = bookRepository.save(b);
        bookId = saved.getId();
    }

    // ---- Reserve ----

    @Test
    void createReservation_withValidToken_returns201() throws Exception {
        mockMvc.perform(post("/api/reservations")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", bookId))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reservationId").isNotEmpty())
            .andExpect(jsonPath("$.bookId").value(bookId.toString()))
            .andExpect(jsonPath("$.status").value("RESERVED"))
            .andExpect(jsonPath("$.expiresAt").isNotEmpty())
            .andExpect(jsonPath("$.message").value("Book reserved successfully. Please pick up within 7 days."));
    }

    @Test
    void createReservation_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", bookId))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createReservation_decrementsCopies() throws Exception {
        int before = bookRepository.findById(bookId).orElseThrow().getAvailableCopies();

        mockMvc.perform(post("/api/reservations")
            .header("Authorization", "Bearer " + patronToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("bookId", bookId))));

        int after = bookRepository.findById(bookId).orElseThrow().getAvailableCopies();
        assert after == before - 1;
    }

    @Test
    void createReservation_bookUnavailable_returns400() throws Exception {
        // Set available copies to 0
        Book b = bookRepository.findById(bookId).orElseThrow();
        b.setAvailableCopies(0);
        bookRepository.save(b);

        mockMvc.perform(post("/api/reservations")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", bookId))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("BOOK_UNAVAILABLE"))
            .andExpect(jsonPath("$.availableCopies").value(0));
    }

    @Test
    void createReservation_atLimit_returns400() throws Exception {
        // Create 5 books and reserve each
        for (int i = 0; i < 5; i++) {
            Book extra = new Book();
            extra.setIsbn("limit-isbn-" + i);
            extra.setTitle("Limit Book " + i);
            extra.setAuthor("Author");
            extra.setTotalCopies(1);
            extra.setAvailableCopies(1);
            Book saved = bookRepository.save(extra);

            mockMvc.perform(post("/api/reservations")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", saved.getId()))));
        }

        // 6th reservation should fail
        mockMvc.perform(post("/api/reservations")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", bookId))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("RESERVATION_LIMIT_EXCEEDED"))
            .andExpect(jsonPath("$.currentReservations").value(5));
    }

    // ---- Active Reservations ----

    @Test
    void getActiveReservations_returns200WithDaysUntilExpiry() throws Exception {
        // Make a reservation first
        mockMvc.perform(post("/api/reservations")
            .header("Authorization", "Bearer " + patronToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("bookId", bookId))));

        mockMvc.perform(get("/api/reservations")
                .header("Authorization", "Bearer " + patronToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalActive").value(1))
            .andExpect(jsonPath("$.reservations[0].status").value("RESERVED"))
            .andExpect(jsonPath("$.reservations[0].daysUntilExpiry").value(7));
    }

    // ---- Full lifecycle: reserve → checkout → return ----

    @Test
    void fullLifecycle_reserveCheckoutReturn_onTime() throws Exception {
        // 1. Reserve
        MvcResult resResult = mockMvc.perform(post("/api/reservations")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", bookId))))
            .andReturn();

        String reservationId = objectMapper.readTree(resResult.getResponse().getContentAsString())
            .get("reservationId").asText();

        // 2. Checkout (LIBRARIAN)
        mockMvc.perform(post("/api/reservations/" + reservationId + "/checkout")
                .header("Authorization", "Bearer " + librarianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CHECKED_OUT"))
            .andExpect(jsonPath("$.dueDate").isNotEmpty())
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Due date:")));

        // 3. Return (LIBRARIAN)
        mockMvc.perform(post("/api/reservations/" + reservationId + "/return")
                .header("Authorization", "Bearer " + librarianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("condition", "GOOD"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lateDays").value(0))
            .andExpect(jsonPath("$.lateFee").value(0.0))
            .andExpect(jsonPath("$.message").value("Book returned successfully"));

        // Verify book copies restored
        int copies = bookRepository.findById(bookId).orElseThrow().getAvailableCopies();
        assert copies == 5; // started at 5, decremented, then restored
    }

    // ---- Checkout role restriction ----

    @Test
    void checkout_asPatron_returns403() throws Exception {
        // First create a reservation to have a reservationId
        MvcResult res = mockMvc.perform(post("/api/reservations")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", bookId))))
            .andReturn();
        String reservationId = objectMapper.readTree(res.getResponse().getContentAsString())
            .get("reservationId").asText();

        mockMvc.perform(post("/api/reservations/" + reservationId + "/checkout")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    // ---- Return role restriction ----

    @Test
    void return_asPatron_returns403() throws Exception {
        mockMvc.perform(post("/api/reservations/" + UUID.randomUUID() + "/return")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("condition", "GOOD"))))
            .andExpect(status().isForbidden());
    }

    // ---- Invalid status transitions ----

    @Test
    void checkout_alreadyCheckedOut_returns400() throws Exception {
        // Reserve then checkout
        MvcResult res = mockMvc.perform(post("/api/reservations")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", bookId))))
            .andReturn();
        String reservationId = objectMapper.readTree(res.getResponse().getContentAsString())
            .get("reservationId").asText();

        mockMvc.perform(post("/api/reservations/" + reservationId + "/checkout")
            .header("Authorization", "Bearer " + librarianToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"));

        // Try to checkout again
        mockMvc.perform(post("/api/reservations/" + reservationId + "/checkout")
                .header("Authorization", "Bearer " + librarianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_STATUS"))
            .andExpect(jsonPath("$.currentStatus").value("CHECKED_OUT"));
    }

    @Test
    void return_notCheckedOut_returns400() throws Exception {
        // Reserve but don't checkout
        MvcResult res = mockMvc.perform(post("/api/reservations")
                .header("Authorization", "Bearer " + patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("bookId", bookId))))
            .andReturn();
        String reservationId = objectMapper.readTree(res.getResponse().getContentAsString())
            .get("reservationId").asText();

        mockMvc.perform(post("/api/reservations/" + reservationId + "/return")
                .header("Authorization", "Bearer " + librarianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("condition", "GOOD"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_STATUS"));
    }

    // ---- Borrowing history ----

    @Test
    void borrowingHistory_returns200WithPagination() throws Exception {
        mockMvc.perform(get("/api/reservations/history")
                .header("Authorization", "Bearer " + patronToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").isNumber());
    }

    // ---- Helpers ----

    private String loginAs(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("accessToken").asText();
    }

    private String registerAndLogin(String email, String password) throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail(email);
        reg.setPassword(password);
        reg.setFirstName("Test");
        reg.setLastName("User");
        reg.setPhoneNumber("+1-555-0200");

        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reg)));

        return loginAs(email, password);
    }
}
