package com.system.vetcare.service;

import com.system.vetcare.domain.UserPrincipal;
import com.system.vetcare.domain.User;
import com.system.vetcare.payload.request.UserRegistrationRequest;

public interface UserService {
    
    UserPrincipal save(UserRegistrationRequest userRegistrationRequest);

    void updateLoginTimestamp(User user);

    User loadUserByUsername(String email);
    
}