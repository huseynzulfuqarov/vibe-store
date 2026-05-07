package com.example.vibe_store.service;

import com.example.vibe_store.dto.auth.*;

import java.util.List;

public interface AuthService {

    AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(String accessTokenHeader, String refreshTokenRaw);
    AuthResponse googleLogin(GoogleLoginRequestDTO request, String deviceInfo, String ipAddress);
    void createAdmin(CreateAdminRequestDTO request);
    void changeEmployeeRole(ChangeRoleRequestDTO request);
    void changePassword(ChangePasswordRequestDTO request, String username);
    List<SessionDTO> getActiveSessions(String username);
    void revokeSession(Long sessionId, String username);
    void revokeAllSessions(String username);
}
