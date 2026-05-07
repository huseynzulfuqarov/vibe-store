package com.example.vibe_store.service;

import com.example.vibe_store.dto.auth.SessionDTO;
import com.example.vibe_store.entity.RefreshToken;
import com.example.vibe_store.entity.User;

import java.util.List;

public interface RefreshTokenService {

    RefreshToken create(User user, String rawToken, String deviceInfo, String ipAddress);

    RefreshToken validateAndGet(String rawToken);

    RefreshToken rotate(RefreshToken old, String newRawToken);

    void revoke(String rawToken);

    int revokeAllForUser(User user);

    List<SessionDTO> listActiveSessions(User user);

    void revokeSessionById(Long sessionId, User user);
}