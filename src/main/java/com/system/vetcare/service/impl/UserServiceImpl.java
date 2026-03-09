package com.system.vetcare.service.impl;

import static java.time.LocalDateTime.now;
import static java.lang.String.format;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.system.vetcare.domain.UserPrincipal;
import com.system.vetcare.domain.User;
import com.system.vetcare.payload.request.UserRegistrationRequest;
import com.system.vetcare.repository.UserRepository;
import com.system.vetcare.service.AuthorityService;
import com.system.vetcare.service.UserService;
import com.system.vetcare.service.strategy.PrincipalProfileResolver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static final String USER_EMAIL_DOES_NOT_EXISTS = "The user with email: %s is doesn't exists.";
    
    private final UserRepository userRepository;
    private final AuthorityService authorityService;
    private final PrincipalProfileResolver userProfileResolver;
    private final PasswordEncoder passwordEncoder;
   
    @Override
    @Transactional
    public UserPrincipal save(UserRegistrationRequest userRegistrationRequest) {   
        User user = userRepository
                .save(User
                        .builder()
                        .firstName(userRegistrationRequest.firstName())
                        .lastName(userRegistrationRequest.lastName())
                        .email(userRegistrationRequest.email())
                        .password(passwordEncoder.encode(userRegistrationRequest.password()))
                        .legalCertificateId(userRegistrationRequest.legalCertificateId())
                        .authorities(authorityService.findAllById(userRegistrationRequest.authorityIds()))
                        .accountNonLocked(true)
                        .lastLogin(now())
                        .build()); 
        userProfileResolver.saveUserProfiles(user);
        return new UserPrincipal(user);
    }

    @Override
    public User loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(format(USER_EMAIL_DOES_NOT_EXISTS, email)));
    }

    @Override
    @Transactional
    public void updateLoginTimestamp(User user) {
        user.setLastLogin(now());
    }
   
}