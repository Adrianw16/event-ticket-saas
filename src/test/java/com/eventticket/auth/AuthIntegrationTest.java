package com.eventticket.auth;

import com.eventticket.transferobject.RegisterRequest;
import com.eventticket.transferobject.AuthResponse;
import com.eventticket.transferobject.LoginRequest;
import com.eventticket.model.Organization;
import com.eventticket.model.User;
import com.eventticket.repository.OrganizationRepository;
import com.eventticket.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void testFullAuthFlow_RegisterLoginAccessProtected() throws Exception {
        // -- STEP 1: Register new organizer
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("organizer@example.com");
        registerRequest.setPassword("SecurePassword123");
        registerRequest.setOrganizationName("My Event Org");

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse registerAuth = objectMapper.readValue(registerResponse, AuthResponse.class);
        String registerToken = registerAuth.getToken();

        // -- STEP 2: Access protected route with register token
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + registerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("organizer@example.com"));

        // -- STEP 3: Login with same credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("organizer@example.com");
        loginRequest.setPassword("SecurePassword123");

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse loginAuth = objectMapper.readValue(loginResponse, AuthResponse.class);
        String loginToken = loginAuth.getToken();

        // -- STEP 4: Access protected route with login token
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("organizer@example.com"));
    }

    @Test
    void testProtectedRouteWithoutToken() throws Exception {
        // -- Try to access protected endpoint without token → 401
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedRouteWithMalformedToken() throws Exception {
        // -- Try with malformed JWT → 401
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer notavalidjwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginWithWrongPassword() throws Exception {
        // -- Setup: register user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("user@example.com");
        registerRequest.setPassword("CorrectPassword");
        registerRequest.setOrganizationName("Test Org");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // -- Try to login with wrong password → 401
        LoginRequest wrongPasswordRequest = new LoginRequest();
        wrongPasswordRequest.setEmail("user@example.com");
        wrongPasswordRequest.setPassword("WrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized());
    }
}