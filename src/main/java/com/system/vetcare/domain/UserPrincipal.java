package com.system.vetcare.domain;

import static java.util.stream.Collectors.toSet;
import java.time.LocalDateTime;
import java.util.Set;

public record UserPrincipal(
        Integer id,
        String email,
        LocalDateTime lastLogin,
        Set<String> authorityNames) {

    public UserPrincipal(User user) {
        this(user.getId(), 
             user.getEmail(), 
             user.getLastLogin(), 
             user.getAuthorities()
                 .stream()
                 .map(authority -> authority.getTitle().name())
                 .collect(toSet()));
    }
    
}