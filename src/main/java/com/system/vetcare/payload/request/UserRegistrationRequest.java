package com.system.vetcare.payload.request;

import java.util.List;

public record UserRegistrationRequest(
        String firstName, 
        String lastName, 
        String email, 
        String password,
        String legalCertificateId, 
        List<Integer> authorityIds) {
}