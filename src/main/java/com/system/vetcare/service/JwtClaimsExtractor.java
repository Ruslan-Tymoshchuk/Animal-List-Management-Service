package com.system.vetcare.service;

import java.util.Set;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Claims;

public interface JwtClaimsExtractor {

    Claims extractAccessTokenClaims(String token);

    Claims extractRefreshTokenClaims(String token);

    String extractEmail(Claims claims);

    Set<SimpleGrantedAuthority> extractAuthorities(Claims claims);

}