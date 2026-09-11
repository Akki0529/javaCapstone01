package assembly.general.api.integration;

import assembly.general.api.dto.request.LoginRequest;
import assembly.general.api.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---- Registration ----

    @Test
    void register_withValidData_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("newuser@example.com");
        req.setPassword("SecurePass123!");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPhoneNumber("+1-555-0100");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").isNotEmpty())
            .andExpect(jsonPath("$.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.role").value("PATRON"))
            .andExpect(jsonPath("$.membershipStatus").value("ACTIVE"))
            .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        RegisterRequest req = validRegisterRequest("dup@example.com");
        // first registration
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)));

        // duplicate
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void register_weakPassword_returns400() throws Exception {
        RegisterRequest req = validRegisterRequest("weak@example.com");
        req.setPassword("weak"); // fails validation

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    // ---- Login ----

    @Test
    void login_withValidCredentials_returns200WithToken() throws Exception {
        // Register first
        RegisterRequest reg = validRegisterRequest("login@example.com");
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setEmail("login@example.com");
        login.setPassword("SecurePass123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(86400))
            .andExpect(jsonPath("$.user.email").value("login@example.com"))
            .andExpect(jsonPath("$.user.role").value("PATRON"));
    }

    @Test
    void login_withInvalidPassword_returns401() throws Exception {
        RegisterRequest reg = validRegisterRequest("bad@example.com");
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setEmail("bad@example.com");
        login.setPassword("WrongPassword1!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("AUTHENTICATION_FAILED"));
    }

    @Test
    void login_withNonexistentEmail_returns401() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("ghost@example.com");
        login.setPassword("SecurePass123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isUnauthorized());
    }

    // ---- Profile ----

    @Test
    void profile_withValidToken_returns200() throws Exception {
        String token = registerAndLogin("profile@example.com");

        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("profile@example.com"))
            .andExpect(jsonPath("$.role").value("PATRON"))
            .andExpect(jsonPath("$.activeReservations").value(0))
            .andExpect(jsonPath("$.borrowingHistory").value(0));
    }

    @Test
    void profile_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
            .andExpect(status().isUnauthorized());
    }

    // ---- Helpers ----

    private RegisterRequest validRegisterRequest(String email) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword("SecurePass123!");
        req.setFirstName("Test");
        req.setLastName("User");
        req.setPhoneNumber("+1-555-0999");
        return req;
    }

    String registerAndLogin(String email) throws Exception {
        RegisterRequest reg = validRegisterRequest(email);
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword("SecurePass123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("accessToken").asText();
    }
}
