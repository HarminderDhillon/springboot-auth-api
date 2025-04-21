package com.dhillon.authapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "users")
public record User(
    @Id String id,
    String username,
    String email,
    String password,
    boolean enabled,
    Set<String> roles
) {}
