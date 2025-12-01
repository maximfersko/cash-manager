package com.fersko.authservice.dto;

public record UserResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        Boolean emailVerified
) {
}
