package com.manager.animallist.payload;

import java.util.Set;
import lombok.Data;

@Data
public class RegistrationRequest {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;
    private final Set<String> roleNames;
    
}