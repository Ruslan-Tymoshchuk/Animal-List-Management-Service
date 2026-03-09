package com.system.vetcare.payload.response;

import java.util.*;

public record AuthenticationResponse(
        String email, 
        String lastLogin,
        List<PrincipalProfile> userAuthorities) {
}