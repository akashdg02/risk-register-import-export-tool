package com.internship.tool.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
