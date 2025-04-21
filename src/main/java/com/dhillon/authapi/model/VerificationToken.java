package com.dhillon.authapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "verification_tokens")
public record VerificationToken(
    @Id String id,
    String token,
    String userId,
    Date expiryDate
) {}
