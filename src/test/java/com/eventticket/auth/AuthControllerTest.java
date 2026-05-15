package com.eventticket.auth;

import com.eventticket.model.Organization;
import com.eventticket.model.User;
import com.eventticket.repository.OrganizationRepository;
import com.eventticket.repository.UserRepository;
import com.eventticket.transferobject.LoginRequest;
import com.eventticket.transferobject.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

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
        // -- Clear tables before each test
        userRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void testRegisterSuccessfully() throws Exception {
        // -- Test: register with valid data → 201 + JWT token
        RegisterRequest request = new RegisterRequest();
        request.setEmail("neworg@example.com");
        request.setPassword("SecurePassword123");
        request.setOrganizationName("New Org");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // -- 201
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.orgId").isNumber())
                .andExpect(jsonPath("$.email").value("neworg@example.com"))
                .andExpect(jsonPath("$.token").value(notNullValue()));
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        // -- Setup: create user already in DB
        Organization org = new Organization();
        org.setName("Existing Org");
        org.setEmail("existing@example.com");
        org.setSlug(org.getName().toLowerCase().replace(" ", "-"));
        Organization savedOrg = organizationRepository.save(org);

        User user = new User();
        user.setOrganization(savedOrg);
        user.setEmail("existing@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123"));
        userRepository.save(user);

        // -- Try to register with same email → 409 Conflict
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("DifferentPassword");
        request.setOrganizationName("Different Org");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()); // -- 409
    }

    @Test
    void testLoginSuccessfully() throws Exception {
        // -- Setup: create a registered user
        Organization org = new Organization();
        org.setName("Test Org");
        org.setEmail("test@example.com");
        org.setSlug(org.getName().toLowerCase().replace(" ", "-"));
        Organization savedOrg = organizationRepository.save(org);

        User user = new User();
        user.setOrganization(savedOrg);
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("CorrectPassword"));
        userRepository.save(user);

        // -- Login with correct password → 200 + JWT
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("CorrectPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // -- 200
                .andExpect(jsonPath("$.token").value(notNullValue()));
    }

    @Test
    void testLoginInvalidPassword() throws Exception {
        // -- Setup: create a user
        Organization org = new Organization();
        org.setName("Test Org");
        org.setEmail("test@example.com");
        org.setSlug(org.getName().toLowerCase().replace(" ", "-"));
        Organization savedOrg = organizationRepository.save(org);

        User user = new User();
        user.setOrganization(savedOrg);
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("CorrectPassword"));
        userRepository.save(user);

        // -- Try login with wrong password → 401
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // -- 401
    }
}