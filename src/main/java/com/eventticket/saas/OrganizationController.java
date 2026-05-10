package com.eventticket.saas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationRepository organizationRepository;

    @GetMapping
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        return ResponseEntity.ok(organizationRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization organization) {
        Organization createdOrganization = organizationRepository.save(organization);
        return ResponseEntity.status(201).body(createdOrganization);
    }
}
