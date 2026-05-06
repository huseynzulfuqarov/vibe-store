package com.example.vibe_store.service;

import com.example.vibe_store.dto.auth.*;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(String authHeader);

    void createAdmin(CreateAdminRequestDTO request);

    void changeEmployeeRole(ChangeRoleRequestDTO request);

    void changePassword(ChangePasswordRequestDTO request, String username);

    AuthResponse googleLogin(GoogleLoginRequestDTO request);
}
