package com.eventticket.repository;

import com.eventticket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //Find user by org id and email (for multi-tenancy)
    Optional<User> findByOrganizationIdAndEmail(Long orgId, String email);
    //Find user by email across anu org (for login before we know the org)
    Optional<User> findByEmail(String email);
}
