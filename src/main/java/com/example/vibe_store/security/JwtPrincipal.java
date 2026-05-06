package com.example.vibe_store.security;

public record JwtPrincipal(String username, Integer employeeId, Integer storeId) {

    @Override
    public String toString() {
        return username;
    }
}
