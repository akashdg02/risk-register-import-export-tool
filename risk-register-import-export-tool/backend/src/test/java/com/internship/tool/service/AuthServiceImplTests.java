package com.internship.tool.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.internship.tool.config.JwtUtil;
import com.internship.tool.dto.AuthLoginRequest;
import com.internship.tool.dto.AuthRefreshRequest;
import com.internship.tool.dto.AuthRegisterRequest;
import com.internship.tool.dto.AuthResponse;
import com.internship.tool.entity.UserAccount;
import com.internship.tool.exception.AuthenticationFailedException;
import com.internship.tool.exception.DuplicateUserException;
import com.internship.tool.repository.UserAccountRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
            userAccountRepository,
            passwordEncoder,
            authenticationManager,
            userDetailsService,
            jwtUtil,
            validator
        );
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        AuthRegisterRequest request = createRegisterRequest();
        UserAccount savedUser = createUserAccount();
        UserDetails userDetails = createUserDetails();

        when(userAccountRepository.existsByEmailIgnoreCase("anagha@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("encoded-password");
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername("anagha@example.com")).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(response.getEmail()).isEqualTo("anagha@example.com");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void shouldThrowConflictWhenRegisteringExistingUser() {
        when(userAccountRepository.existsByEmailIgnoreCase("anagha@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(createRegisterRequest()))
            .isInstanceOf(DuplicateUserException.class)
            .hasMessageContaining("anagha@example.com");
    }

    @Test
    void shouldLoginSuccessfully() {
        AuthLoginRequest request = createLoginRequest();
        UserAccount user = createUserAccount();
        UserDetails userDetails = createUserDetails();

        when(userAccountRepository.findByEmailIgnoreCase("anagha@example.com")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("anagha@example.com")).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(
            new UsernamePasswordAuthenticationToken("anagha@example.com", "SecurePass123")
        );
        assertThat(response.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void shouldThrowUnauthorizedWhenLoginFails() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(createLoginRequest()))
            .isInstanceOf(AuthenticationFailedException.class)
            .hasMessage("Invalid email or password");
    }

    @Test
    void shouldRefreshTokensSuccessfully() {
        UserAccount user = createUserAccount();
        UserDetails userDetails = createUserDetails();
        AuthRefreshRequest request = new AuthRefreshRequest();
        request.setRefreshToken("refresh-token");

        when(jwtUtil.extractUsername("refresh-token")).thenReturn("anagha@example.com");
        when(userAccountRepository.findByEmailIgnoreCase("anagha@example.com")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("anagha@example.com")).thenReturn(userDetails);
        when(jwtUtil.isRefreshTokenValid("refresh-token", userDetails)).thenReturn(true);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn("new-refresh-token");
        when(jwtUtil.getAccessTokenExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.refreshToken(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void shouldRejectInvalidRefreshToken() {
        AuthRefreshRequest request = new AuthRefreshRequest();
        request.setRefreshToken("bad-token");

        when(jwtUtil.extractUsername("bad-token")).thenThrow(new IllegalArgumentException("Invalid token"));

        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(AuthenticationFailedException.class)
            .hasMessage("Refresh token is invalid");
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

    private UserAccount createUserAccount() {
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setFullName("Anagha");
        user.setEmail("anagha@example.com");
        user.setPasswordHash("encoded-password");
        user.setRole("ROLE_USER");
        user.setActive(true);
        return user;
    }

    private UserDetails createUserDetails() {
        return User.builder()
            .username("anagha@example.com")
            .password("encoded-password")
            .roles("USER")
            .build();
    }
}
