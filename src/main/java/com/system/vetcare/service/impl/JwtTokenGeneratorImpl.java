package com.system.vetcare.service.impl;

import static java.lang.System.*;
import static com.system.vetcare.service.constants.JwtClaimKeys.*;
import java.time.Duration;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.system.vetcare.service.JwtTokenGenerator;
import java.util.Date;
import java.util.Set;
import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
public class JwtTokenGeneratorImpl implements JwtTokenGenerator {
    
    @Value("${jwt.issuer}")
    private String jwtIssuer;
    @Value("${access.token.life.time}")
    private Duration accessTokenLifeTime;
    @Value("${refresh.token.life.time}")
    private Duration refreshTokenLifeTime; 
    
    private final SecretKey accessTokenSecretKey;
    private final SecretKey refreshTokenSecretKey;

    @Override
    public String generateAccessToken(String userEmail, Set<String> authorityNames) {
        return Jwts
                 .builder()
                 .issuer(jwtIssuer)
                 .subject(userEmail)
                 .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                 .claim(AUTHORITIES_CLAIM, authorityNames)
                 .issuedAt(new Date(currentTimeMillis()))
                 .expiration(new Date(currentTimeMillis() + accessTokenLifeTime.toMillis()))
                 .signWith(accessTokenSecretKey)
                 .compact();
    }
    
    @Override
    public String generateRefreshToken(String userEmail) {
        return Jwts
                 .builder()
                 .issuer(jwtIssuer)
                 .subject(userEmail)
                 .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                 .issuedAt(new Date(currentTimeMillis()))
                 .expiration(new Date(currentTimeMillis() + refreshTokenLifeTime.toMillis()))
                 .signWith(refreshTokenSecretKey)
                 .compact();
    }

}