package com.system.vetcare.service.strategy;

import com.system.vetcare.domain.User;
import com.system.vetcare.domain.enums.EAuthority;
import com.system.vetcare.payload.response.PrincipalProfile;

public interface PrincipalProfileResolverStrategy {

    EAuthority getSupportedAuthority();
    
    PrincipalProfile resolveUserProfileDetails(Integer userId);

    void saveProfileForUser(User user);
    
}