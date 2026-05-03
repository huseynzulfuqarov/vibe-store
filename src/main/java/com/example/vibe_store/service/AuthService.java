package com.example.vibe_store.service;

import com.example.vibe_store.dto.auth.AuthResponse;
import com.example.vibe_store.dto.auth.LoginRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(String authHeader);
}
