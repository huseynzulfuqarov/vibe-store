package com.example.vibe_store.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = getClientIp(request);
        String uri = request.getRequestURI();

        String key;
        Bucket bucket;

        if (uri.startsWith("/api/auth/login") || uri.startsWith("/api/auth/google") || uri.startsWith("/api/auth/refresh")) {
            key = ip + ":auth";
            bucket = cache.computeIfAbsent(key, k -> createBucket(5, Duration.ofMinutes(1)));
        } else if (uri.startsWith("/api/ai")) {
            String username = null;
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    String token = header.substring(7);
                    if (jwtTokenProvider.validateToken(token)) {
                        username = jwtTokenProvider.extractUsername(token);
                    }
                } catch (Exception e) {
                    //test
                }
            }
            key = (username != null ? username : ip) + ":ai";
            bucket = cache.computeIfAbsent(key, k -> createBucket(10, Duration.ofMinutes(1)));
        } else {
            key = ip + ":general";
            bucket = cache.computeIfAbsent(key, k -> createBucket(60, Duration.ofMinutes(1)));
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Please try again later.\"}");
        }
    }

    private Bucket createBucket(long capacity, Duration duration) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.greedy(capacity, duration)))
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}