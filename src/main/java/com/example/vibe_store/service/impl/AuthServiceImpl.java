package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.auth.*;
import com.example.vibe_store.entity.RefreshToken;
import com.example.vibe_store.entity.User;
import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.enums.Role;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.EmployeeRepository;
import com.example.vibe_store.repository.UserRepository;
import com.example.vibe_store.security.CustomUserDetails;
import com.example.vibe_store.security.GoogleAuthenticationToken;
import com.example.vibe_store.security.JwtTokenProvider;
import com.example.vibe_store.security.TokenBlacklistService;
import com.example.vibe_store.service.AuthService;
import com.example.vibe_store.repository.EmployeeWorkHistoryRepository;
import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import com.example.vibe_store.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeWorkHistoryRepository employeeWorkHistoryRepository;
    private final TokenBlacklistService blacklistService;
    private final RefreshTokenService refreshTokenService;


    @Override
    public AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.userName(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        refreshTokenService.create(user, refreshToken, deviceInfo, ipAddress);

        return new AuthResponse(accessToken, refreshToken);
    }


    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String rawRefreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(rawRefreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        if (!"refresh".equals(jwtTokenProvider.extractTokenType(rawRefreshToken))) {
            throw new BadCredentialsException("Not a refresh token");
        }

       RefreshToken storedToken = refreshTokenService.validateAndGet(rawRefreshToken);

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService
                .loadUserByUsername(storedToken.getUser().getUsername());
        String newAccess = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefresh = jwtTokenProvider.generateRefreshToken(userDetails);

        refreshTokenService.rotate(storedToken, newRefresh);

        return new AuthResponse(newAccess, newRefresh);
    }


    @Override
    public void logout(String accessTokenHeader, String refreshTokenRaw) {
        String accessToken = jwtTokenProvider.extractToken(accessTokenHeader);
        blacklistService.addToBlacklist(accessToken);

        if (refreshTokenRaw != null && !refreshTokenRaw.isBlank()) {
            try {
                refreshTokenService.revoke(refreshTokenRaw);
            } catch (Exception e) {
                log.warn("Could not revoke refresh token: {}", e.getMessage());
            }
        }

        log.info("User logged out");
    }


    @Override
    @Transactional
    public void createAdmin(CreateAdminRequestDTO request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }

        User admin = new User();
        admin.setUsername(request.username());
        admin.setEmail(request.email());
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        log.info("New admin created: {}", request.username());
    }

    @Override
    @Transactional
    public void changeEmployeeRole(ChangeRoleRequestDTO request) {
        if (request.newRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot assign ADMIN role to an employee. Use createAdmin instead.");
        }

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + request.employeeId()));

        User user = userRepository.findByEmployee(employee)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found for employee: " + request.employeeId()));

        Role oldRole = user.getRole();
        user.setRole(request.newRole());
        
        if (request.newRole() == Role.MANAGER) {
             EmployeeWorkHistory activeHistory = employeeWorkHistoryRepository.findByEmployeeIdAndIsActiveTrue(employee.getId())
                     .orElseThrow(() -> new ResourceNotFoundException("Active work history not found"));
             user.setStore(activeHistory.getStore());
        }
        
        userRepository.save(user);

        log.info("Employee {} role changed: {} → {}", request.employeeId(), oldRole, request.newRole());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequestDTO request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("User {} changed their password", username);
    }

    @Override
    public AuthResponse googleLogin(GoogleLoginRequestDTO request,
                                    String deviceInfo, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
                new GoogleAuthenticationToken(request.idToken())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        refreshTokenService.create(user, refreshToken, deviceInfo, ipAddress);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public List<SessionDTO> getActiveSessions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return refreshTokenService.listActiveSessions(user);
    }

    @Override
    public void revokeSession(Long sessionId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        refreshTokenService.revokeSessionById(sessionId, user);
    }

    @Override
    @Transactional
    public void revokeAllSessions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        refreshTokenService.revokeAllForUser(user);
    }
}
