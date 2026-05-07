package com.example.vibe_store.controller.auth;

import com.example.vibe_store.dto.auth.*;
import com.example.vibe_store.security.DeviceInfoParser;
import com.example.vibe_store.security.JwtPrincipal;
import com.example.vibe_store.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final DeviceInfoParser deviceInfoParser;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody @Valid GoogleLoginRequestDTO request,
                                                    HttpServletRequest httpRequest) {
        String deviceInfo = deviceInfoParser.parse(httpRequest.getHeader("User-Agent"));
        String ipAddress = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(authService.googleLogin(request, deviceInfo, ipAddress));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request,
                                              HttpServletRequest httpRequest) {
        String deviceInfo = deviceInfoParser.parse(httpRequest.getHeader("User-Agent"));
        String ipAddress = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(authService.login(request, deviceInfo, ipAddress));
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody(required = false) LogoutRequest body) {
        String refreshToken = (body != null) ? body.refreshToken() : null;
        authService.logout(authHeader, refreshToken);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<Void> createAdmin(@RequestBody @Valid CreateAdminRequestDTO request) {
        authService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/role")
    public ResponseEntity<Void> changeRole(@RequestBody @Valid ChangeRoleRequestDTO request) {
        authService.changeEmployeeRole(request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequestDTO request,
                                                Authentication authentication) { //Spring MVC controller metodunu ArgumentResolver ile gonderir.
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        authService.changePassword(request, principal.username());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionDTO>> getActiveSessions(Authentication auth) {
        JwtPrincipal principal = (JwtPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(authService.getActiveSessions(principal.username()));
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> revokeSession(@PathVariable Long id, Authentication auth) {
        JwtPrincipal principal = (JwtPrincipal) auth.getPrincipal();
        authService.revokeSession(id, principal.username());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Void> revokeAllSessions(Authentication auth) {
        JwtPrincipal principal = (JwtPrincipal) auth.getPrincipal();
        authService.revokeAllSessions(principal.username());
        return ResponseEntity.noContent().build();
    }
}
