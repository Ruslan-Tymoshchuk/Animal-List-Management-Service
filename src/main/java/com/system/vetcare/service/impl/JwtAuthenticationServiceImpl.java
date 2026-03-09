package com.system.vetcare.service.impl;

import org.springframework.stereotype.Service;
import com.system.vetcare.domain.UserPrincipal;
import com.system.vetcare.domain.JwtAuthenticationToken;
import com.system.vetcare.domain.User;
import com.system.vetcare.service.JwtAuthenticationService;
import com.system.vetcare.service.JwtClaimsExtractor;
import com.system.vetcare.service.JwtTokenBlacklistService;
import com.system.vetcare.service.JwtTokenGenerator;
import com.system.vetcare.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationServiceImpl implements JwtAuthenticationService {

    public static final String JWT_REFRESH_TOKEN_ALREADY_REVOKED = "JWT refresh token has already been revoked";
    
    private final JwtTokenGenerator jwtTokenGenerator;
    private final JwtTokenBlacklistService jwtTokenBlacklistService;
    private final JwtClaimsExtractor jwtClaimsExtractor;
    private final UserService userService;
    
    @Override
    public JwtAuthenticationToken issueAuthenticationToken(UserPrincipal authenticatedPrincipal) {
        final String email = authenticatedPrincipal.email();
        final String accessToken = jwtTokenGenerator.generateAccessToken(email, authenticatedPrincipal.authorityNames());
        final String refreshToken = jwtTokenGenerator.generateRefreshToken(email);
        return new JwtAuthenticationToken(accessToken, refreshToken);
    }

    @Override
    public JwtAuthenticationToken refreshAuthenticationToken(String jwtRefreshToken) {
        if (!jwtTokenBlacklistService.isBlacklisted(jwtRefreshToken)) {
            final Claims claims = jwtClaimsExtractor.extractRefreshTokenClaims(jwtRefreshToken);
            jwtTokenBlacklistService.addTokenToBlacklist(jwtRefreshToken);
            final String email = jwtClaimsExtractor.extractEmail(claims);
            final User user = userService.loadUserByUsername(email);
            return issueAuthenticationToken(new UserPrincipal(user));
        } else {
            throw new JwtException(JWT_REFRESH_TOKEN_ALREADY_REVOKED);
        }
    }
    
    @Override
    public void revokeAuthenticationToken(String jwtRefreshToken) {
        jwtTokenBlacklistService.addTokenToBlacklist(jwtRefreshToken);
    }
    
}