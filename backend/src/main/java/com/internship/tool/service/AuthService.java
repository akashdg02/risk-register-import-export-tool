package com.internship.tool.service;

import com.internship.tool.dto.AuthLoginRequest;
import com.internship.tool.dto.AuthRefreshRequest;
import com.internship.tool.dto.AuthRegisterRequest;
import com.internship.tool.dto.AuthResponse;

public interface AuthService {

    AuthResponse register(AuthRegisterRequest request);

    AuthResponse login(AuthLoginRequest request);

    AuthResponse refreshToken(AuthRefreshRequest request);
}
