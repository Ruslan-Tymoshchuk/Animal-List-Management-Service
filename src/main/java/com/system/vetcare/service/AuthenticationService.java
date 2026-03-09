package com.system.vetcare.service;

import com.system.vetcare.domain.UserPrincipal;
import com.system.vetcare.payload.request.AuthenticationRequest;
import com.system.vetcare.payload.response.AuthenticationResponse;

public interface AuthenticationService {
    
    UserPrincipal authenticate(AuthenticationRequest credential);
    
    AuthenticationResponse buildAuthenticationResponse(UserPrincipal principalDetails);
    
    void revokePrincipalAuthentication();

}