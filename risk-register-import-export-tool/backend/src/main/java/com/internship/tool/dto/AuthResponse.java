package com.internship.tool.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private final String tokenType;
    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;
    private final String email;
    private final String fullName;
    private final String role;
}
