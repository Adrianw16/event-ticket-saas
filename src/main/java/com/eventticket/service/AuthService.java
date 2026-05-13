package com.eventticket.service;

import com.eventticket.transferobject.AuthRequest;
import com.eventticket.transferobject.AuthResponse;
import com.eventticket.service.JwtService;
import com.eventticket.model.Organization;
import com.eventticket.model.User;
import com.eventticket.repository.OrganizationRepository;
import com.eventticket.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new organizer with their organization.
     * Creates a new Organization + User (ORGANIZER role), hashes password, returns JWT.
     */
    public AuthResponse register(AuthRequest request){
        //First check if user exists already
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        //Check if org name already exists
        if(organizationRepository.findByName(request.getOrganizationName()).isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Organization name already exists");
        }

        //Create Organization
        Organization org = new Organization();
        org.setName(request.getOrganizationName());
        org.setEmail(request.getEmail());
        // Generate slug from organization name ("My Event Org" -> "my-event-org")
        org.setSlug(request.getOrganizationName().toLowerCase().replace(" ", "-"));
        Organization savedOrg = organizationRepository.save(org);

        //Create User
        User user = new User();
        user.setOrganization(savedOrg);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.UserRole.ORGANIZER);
        User savedUser = userRepository.save(user);

        //Generate JWT
        String token = jwtService.generateToken(savedUser.getId(), savedOrg.getId(), savedUser.getEmail());

        return new AuthResponse(
                savedUser.getId(),
                savedOrg.getId(),
                savedUser.getEmail(),
                token
        );
    }

    /**
     * Login an organizer with email + password.
     * Validate credentials, returns JWT if valid.
     */
    public AuthResponse login(AuthRequest request){
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // Authenticate user exists AND password matches(Bcrypt comparison)
        if(user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            // Does not leak if email exists (generic 401 returned)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        //Generate JWT with user's org_id
        String token = jwtService.generateToken(
                user.getId(),
                user.getOrganization().getId(),
                user.getEmail()
        );

        return new AuthResponse(
                user.getId(),
                user.getOrganization().getId(),
                user.getEmail(),
                token
        );
    }
}
