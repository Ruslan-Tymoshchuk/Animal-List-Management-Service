package com.system.vetcare.service.impl;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static com.system.vetcare.service.constants.JwtClaimKeys.*;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.system.vetcare.service.JwtClaimsExtractor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtClaimsExtractorImpl implements JwtClaimsExtractor {

    private final SecretKey accessTokenSecretKey;
    private final SecretKey refreshTokenSecretKey;
    
    @Override
    public Claims extractAccessTokenClaims(String token) {
        return getJwtParser(accessTokenSecretKey)
                 .parseSignedClaims(token)
                 .getPayload();
    }
    
    @Override
    public Claims extractRefreshTokenClaims(String token) {
        return getJwtParser(refreshTokenSecretKey)
                 .parseSignedClaims(token)
                 .getPayload();
    }
    
    @Override
    public String extractEmail(Claims claims) {
        String email = claims.getSubject();
        if (StringUtils.hasText(email)) {
            return email;
        } else {
            throw new JwtException("Email claim is missing or blank");
        }  
    }
    
    @Override
    public Set<GrantedAuthority> extractAuthorities(Claims claims) {
        if (claims.get(AUTHORITIES_CLAIM) instanceof List<?> list) {
            return list
                     .stream()
                     .map(String::valueOf)
                     .map(SimpleGrantedAuthority::new)
                     .collect(toUnmodifiableSet());
        } else {
            throw new JwtException("Invalid authorities claim: expected array");
        }
    }
    
    private JwtParser getJwtParser(SecretKey secretKey) {
        return Jwts
                 .parser()
                 .verifyWith(secretKey)
                 .build();
    }
    
}