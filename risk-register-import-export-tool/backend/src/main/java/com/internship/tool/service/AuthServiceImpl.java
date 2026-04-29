package com.internship.tool.service;

import com.internship.tool.config.JwtUtil;
import com.internship.tool.dto.AuthLoginRequest;
import com.internship.tool.dto.AuthRefreshRequest;
import com.internship.tool.dto.AuthRegisterRequest;
import com.internship.tool.dto.AuthResponse;
import com.internship.tool.entity.UserAccount;
import com.internship.tool.exception.AuthenticationFailedException;
import com.internship.tool.exception.DuplicateUserException;
import com.internship.tool.repository.UserAccountRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final String BEARER = "Bearer";

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final Validator validator;

    public AuthServiceImpl(
        UserAccountRepository userAccountRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        UserDetailsService userDetailsService,
        JwtUtil jwtUtil,
        Validator validator
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.validator = validator;
    }

    @Override
    public AuthResponse register(AuthRegisterRequest request) {
        validate(request);
        String email = normalizeEmail(request.getEmail());

        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateUserException("User already exists with email: " + email);
        }

        UserAccount user = new UserAccount();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(DEFAULT_ROLE);
        user.setActive(Boolean.TRUE);

        UserAccount savedUser = userAccountRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        return buildAuthResponse(savedUser, userDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthLoginRequest request) {
        validate(request);
        String email = normalizeEmail(request.getEmail());

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (BadCredentialsException exception) {
            throw new AuthenticationFailedException("Invalid email or password");
        }

        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
            .filter(existingUser -> Boolean.TRUE.equals(existingUser.getActive()))
            .orElseThrow(() -> new AuthenticationFailedException("Active user not found for email: " + email));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        return buildAuthResponse(user, userDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(AuthRefreshRequest request) {
        validate(request);

        String refreshToken = request.getRefreshToken().trim();
        String email;
        try {
            email = normalizeEmail(jwtUtil.extractUsername(refreshToken));
        } catch (RuntimeException exception) {
            throw new AuthenticationFailedException("Refresh token is invalid");
        }

        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
            .filter(existingUser -> Boolean.TRUE.equals(existingUser.getActive()))
            .orElseThrow(() -> new AuthenticationFailedException("Active user not found for refresh token"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        if (!jwtUtil.isRefreshTokenValid(refreshToken, userDetails)) {
            throw new AuthenticationFailedException("Refresh token is invalid or expired");
        }

        return buildAuthResponse(user, userDetails);
    }

    private AuthResponse buildAuthResponse(UserAccount user, UserDetails userDetails) {
        return AuthResponse.builder()
            .tokenType(BEARER)
            .accessToken(jwtUtil.generateAccessToken(userDetails))
            .refreshToken(jwtUtil.generateRefreshToken(userDetails))
            .expiresIn(jwtUtil.getAccessTokenExpirationMs())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole())
            .build();
    }

    private <T> void validate(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining(", "));
            throw new AuthenticationFailedException(message);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
