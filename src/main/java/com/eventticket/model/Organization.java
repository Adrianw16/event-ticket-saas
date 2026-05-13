package com.eventticket.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    //Getters & Setters
    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getSlug() {return slug;}
    public void setSlug(String slug) {this.slug = slug;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public LocalDateTime getCreatedAt() {return createdAt;}

}
