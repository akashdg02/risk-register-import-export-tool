package com.internship.tool.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.config.JwtAuthFilter;
import com.internship.tool.dto.AuthLoginRequest;
import com.internship.tool.dto.AuthRefreshRequest;
import com.internship.tool.dto.AuthRegisterRequest;
import com.internship.tool.dto.AuthResponse;
import com.internship.tool.exception.AuthenticationFailedException;
import com.internship.tool.exception.DuplicateUserException;
import com.internship.tool.exception.GlobalExceptionHandler;
import com.internship.tool.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void shouldRegisterUser() throws Exception {
        when(authService.register(any(AuthRegisterRequest.class))).thenReturn(createResponse());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRegisterRequest())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("anagha@example.com"));
    }

    @Test
    void shouldLoginUser() throws Exception {
        when(authService.login(any(AuthLoginRequest.class))).thenReturn(createResponse());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createLoginRequest())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void shouldRefreshToken() throws Exception {
        when(authService.refreshToken(any(AuthRefreshRequest.class))).thenReturn(createResponse());

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRefreshRequest())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFails() throws Exception {
        when(authService.login(any(AuthLoginRequest.class)))
            .thenThrow(new AuthenticationFailedException("Invalid email or password"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createLoginRequest())))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturnConflictWhenRegisterEmailAlreadyExists() throws Exception {
        when(authService.register(any(AuthRegisterRequest.class)))
            .thenThrow(new DuplicateUserException("User already exists with email: anagha@example.com"));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRegisterRequest())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterRequestIsInvalid() throws Exception {
        AuthRegisterRequest request = createRegisterRequest();
        request.setPassword("123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    private AuthRegisterRequest createRegisterRequest() {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setFullName("Anagha");
        request.setEmail("anagha@example.com");
        request.setPassword("SecurePass123");
        return request;
    }

    private AuthLoginRequest createLoginRequest() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setEmail("anagha@example.com");
        request.setPassword("SecurePass123");
        return request;
    }

    private AuthRefreshRequest createRefreshRequest() {
        AuthRefreshRequest request = new AuthRefreshRequest();
        request.setRefreshToken("refresh-token");
        return request;
    }

    private AuthResponse createResponse() {
        return AuthResponse.builder()
            .tokenType("Bearer")
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .expiresIn(86400000L)
            .email("anagha@example.com")
            .fullName("Anagha")
            .role("ROLE_USER")
            .build();
    }
}
