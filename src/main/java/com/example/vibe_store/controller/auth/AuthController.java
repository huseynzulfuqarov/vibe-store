package com.example.vibe_store.controller.auth;

import com.example.vibe_store.dto.auth.*;
import com.example.vibe_store.security.JwtPrincipal;
import com.example.vibe_store.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody @Valid GoogleLoginRequestDTO request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.refresh(authHeader));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
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
}
