package com.example.vibe_store.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtProvider;

    public void addToBlacklist(String token) {

        long ttl = jwtProvider.extractExpiration(token) - System.currentTimeMillis();
        redisTemplate.opsForValue().set(token, token, ttl, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
