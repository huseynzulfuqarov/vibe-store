package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.auth.SessionDTO;
import com.example.vibe_store.entity.RefreshToken;
import com.example.vibe_store.entity.User;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.RefreshTokenRepository;
import com.example.vibe_store.security.JwtProperties;
import com.example.vibe_store.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public RefreshToken create(User user, String rawToken, String deviceInfo, String ipAddress) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setDeviceInfo(deviceInfo);
        token.setIpAddress(ipAddress);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpiration() / 1000));
        token.setLastUsedAt(LocalDateTime.now());
        return refreshTokenRepository.save(token);
    }

    @Override
    @Transactional
    public RefreshToken validateAndGet(String rawToken) {

        String hash = hashToken(rawToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found"));

        if(storedToken.isRevoked()){
            log.warn("REUSE DETECTED: revoked refresh token used for user id={}", storedToken.getUser().getId());

            revokeAllForUser(storedToken.getUser());
            throw new BadCredentialsException("Token reuse detected — all sessions revoked");
        }

        if (storedToken.isExpired()) {
            throw new BadCredentialsException("Refresh token expired");
        }

        storedToken.setLastUsedAt(LocalDateTime.now());
        return storedToken;
    }

    @Override
    @Transactional
    public RefreshToken rotate(RefreshToken old, String newRawToken) {

        RefreshToken newToken = create(old.getUser(), newRawToken, old.getDeviceInfo(), old.getIpAddress());

        old.setRevoked(true);
        old.setReplacedById(newToken.getId());
        refreshTokenRepository.save(old);
        return  newToken;
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        String hash = hashToken(rawToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found"));
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.info("Refresh token revoked for user id={}", token.getUser().getId());
    }


    @Override
    @Transactional
    public int revokeAllForUser(User user) {
        int count = refreshTokenRepository.revokeAllByUserId(user.getId());
        log.warn("All sessions revoked for user id={}, count={}", user.getId(), count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDTO> listActiveSessions(User user) {
        return refreshTokenRepository
                .findByUserAndRevokedFalseOrderByLastUsedAtDesc(user)
                .stream()
                .filter(t -> !t.isExpired())
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void revokeSessionById(Long sessionId, User user) {
        RefreshToken token = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found: " + sessionId));

        if (!token.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("This session does not belong to you");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.info("Session {} revoked for user id={}", sessionId, user.getId());
    }


    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private SessionDTO toDto(RefreshToken t) {
        return new SessionDTO(
                t.getId(),
                t.getDeviceInfo(),
                t.getIpAddress(),
                t.getCreatedAt(),
                t.getLastUsedAt()
        );
    }
}