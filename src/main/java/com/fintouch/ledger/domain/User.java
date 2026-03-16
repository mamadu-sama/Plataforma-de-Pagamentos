package com.fintouch.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String document;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type;

    protected User() {
    }

    public User(String fullName, String email, String document, UserType type) {
        this.fullName = fullName;
        this.email = email;
        this.document = document;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getDocument() {
        return document;
    }

    public UserType getType() {
        return type;
    }
}

