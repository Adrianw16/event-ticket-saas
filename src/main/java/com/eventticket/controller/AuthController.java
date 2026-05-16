package com.eventticket.controller;

import com.eventticket.model.User;
import com.eventticket.repository.UserRepository;
import com.eventticket.transferobject.UserProfile;
import com.eventticket.transferobject.LoginRequest;
import com.eventticket.transferobject.RegisterRequest;
import com.eventticket.transferobject.AuthResponse;
import com.eventticket.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    /**
     * POST /api/v1/auth/register
     * Accept: { email, password, organizationName }
     * Return: 201 + { userId, orgId, email, token }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
        // Delegate to service (handles org + user creation, hashing, JWT issuance)
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/auth/login
     * @param request user's { email, password }
     * @return 200 + { userId, orgId, email, token } or 401
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        // Delegate to service (validates credentials, generates JWT)
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getCurrentUser(){
        // Extract user ID from SecurityContext
        Long userId = (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = new UserProfile(
                user.getId(),
                user.getEmail(),
                user.getOrganization().getId(),
                user.getRole().toString()
        );

        return ResponseEntity.ok(profile);
    }
}
