package com.example.vibe_store.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class GoogleAuthenticationToken extends AbstractAuthenticationToken {

    private final String idToken;
    private final Object principal;

    public GoogleAuthenticationToken(String idToken) {
        super((Collection<? extends GrantedAuthority>) null);
        this.idToken = idToken;
        this.principal = null;
        setAuthenticated(false);
    }

    public GoogleAuthenticationToken(Object principal,
                                     Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.idToken = null;
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return idToken;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
