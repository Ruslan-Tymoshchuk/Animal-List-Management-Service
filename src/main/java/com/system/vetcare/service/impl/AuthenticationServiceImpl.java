package com.system.vetcare.service.impl;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.lang.String.format;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.system.vetcare.domain.UserPrincipal;
import com.system.vetcare.domain.User;
import com.system.vetcare.payload.request.AuthenticationRequest;
import com.system.vetcare.payload.response.AuthenticationResponse;
import com.system.vetcare.payload.response.PrincipalProfile;
import com.system.vetcare.service.AuthenticationService;
import com.system.vetcare.service.UserService;
import com.system.vetcare.service.strategy.PrincipalProfileResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String ACCOUNT_HAS_BEEN_LOCKED_DUE_TO_FAILED_ATTEMPTS = "Your account has been locked due to %d failed attempts. "
            + "It will be unlocked after %d minutes.";
    public static final String INCORRECT_PASSWORD = "Incorrect password! You taken %s attempts";
    public static final String ACCOUNT_HAS_BEEN_LOCKED = "Your account has been locked";

    @Value("${lock.duration.minutes}")
    private int lockDurationMinutes;
    @Value("${max.failed.attempts}")
    private int maxFailedAttempts;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Map<String, LocalDateTime> accountlockTime = new HashMap<>();   
    private final PrincipalProfileResolver principalProfileResolver;
    
    @Override
    public AuthenticationResponse buildAuthenticationResponse(UserPrincipal principalDetails) {
        List<PrincipalProfile> profiles = principalProfileResolver.resolvePrincipalProfiles(principalDetails);          
        return new AuthenticationResponse(principalDetails.email(), principalDetails.lastLogin().format(TIME_FORMATTER), profiles);
    }
    
    @Override
    public UserPrincipal authenticate(AuthenticationRequest credential) {
        User principal = userService.loadUserByUsername(credential.email());
        if (principal.isAccountNonLocked()) {
            validateCredentials(credential, principal);
            return new UserPrincipal(principal);
        } else {
            throw new LockedException(ACCOUNT_HAS_BEEN_LOCKED);
        }
    }
    
    @Override
    public void revokePrincipalAuthentication() {
        SecurityContextHolder.clearContext();
    }
    
    private void validateCredentials(AuthenticationRequest credential, User principal) {
        if (accountlockTime.containsKey(principal.getEmail()) && accountlockTime.get(principal.getEmail()).isAfter(now())) {
            throw new LockedException(format(ACCOUNT_HAS_BEEN_LOCKED_DUE_TO_FAILED_ATTEMPTS, maxFailedAttempts,
                    MINUTES.between(now(), accountlockTime.get(principal.getEmail())) % 60));
        }
        if (!passwordEncoder.matches(credential.password(), principal.getPassword())) {
            int actualFailedAttempts = failedAttempts.merge(principal.getEmail(), 1, Integer::sum);
            if (failedAttempts.get(principal.getEmail()) == maxFailedAttempts) {
                accountlockTime.put(principal.getEmail(), now().plusMinutes(lockDurationMinutes));
                failedAttempts.put(principal.getEmail(), 0);
            }
            throw new BadCredentialsException(format(INCORRECT_PASSWORD, actualFailedAttempts));
        } else {
            failedAttempts.put(principal.getEmail(), 0);  
            userService.updateLoginTimestamp(principal); 
        }
    }
    
}