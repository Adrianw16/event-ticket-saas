package com.eventticket.repository;

import com.eventticket.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long>{
    Optional<Organization> findByEmail(String email);
    Optional<Organization> findByName(String name);
}
