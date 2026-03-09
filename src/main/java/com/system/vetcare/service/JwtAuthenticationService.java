package com.system.vetcare.service;

import com.system.vetcare.domain.UserPrincipal;
import com.system.vetcare.domain.JwtAuthenticationToken;

public interface JwtAuthenticationService {

    JwtAuthenticationToken issueAuthenticationToken(UserPrincipal authenticatedPrincipal);

    JwtAuthenticationToken refreshAuthenticationToken(String jwtRefreshToken);

    void revokeAuthenticationToken(String jwtRefreshToken);

}