package com.example.vibe_store.security;

import com.example.vibe_store.entity.User;
import com.example.vibe_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthenticationProvider implements AuthenticationProvider {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String idToken = (String) authentication.getCredentials();

        var payload = googleTokenVerifier.verify(idToken);
        String email = payload.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException(
                        "Bu email ilə qeydiyyatda olan istifadəçi tapılmadı. Admin ilə əlaqə saxlayın."));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        log.info("Google ilə daxil oldu: {}", email);
        return new GoogleAuthenticationToken(userDetails, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return GoogleAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
