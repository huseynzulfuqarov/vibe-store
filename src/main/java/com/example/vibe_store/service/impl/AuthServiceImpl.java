package com.example.vibe_store.service.impl;

import com.example.vibe_store.config.ApplicationConfig;
import com.example.vibe_store.dto.auth.AuthResponse;
import com.example.vibe_store.dto.auth.LoginRequest;
import com.example.vibe_store.repository.UserRepository;
import com.example.vibe_store.security.CustomUserDetails;
import com.example.vibe_store.security.JwtTokenProvider;
import com.example.vibe_store.security.UserDetailsServiceImpl;
import com.example.vibe_store.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.userName(), request.password())
        );

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(customUserDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(customUserDetails);
        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refresh(String authHeader) {

        String refreshToken = jwtTokenProvider.extractToken(authHeader);

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid Refresh Token");
        }

        if (!"refresh".equals(jwtTokenProvider.extractTokenType(refreshToken))) {
            throw new BadCredentialsException("Not a refresh token");
        }

        String username = jwtTokenProvider.extractUsername(refreshToken);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService
                .loadUserByUsername(username);

        String newAccess = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefresh = jwtTokenProvider.generateRefreshToken(userDetails);

        return new AuthResponse(newAccess, newRefresh);

    }

    @Override
    public void logout(String authHeader) {

    }
}
