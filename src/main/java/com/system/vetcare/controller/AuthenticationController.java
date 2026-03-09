package com.system.vetcare.controller;

import static com.system.vetcare.controller.constants.AuthenticationUrl.*;
import static org.springframework.http.HttpStatus.*;
import static com.system.vetcare.payload.JwtMarkers.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.system.vetcare.domain.UserPrincipal;
import com.system.vetcare.domain.JwtAuthenticationToken;
import com.system.vetcare.payload.request.AuthenticationRequest;
import com.system.vetcare.payload.request.UserRegistrationRequest;
import com.system.vetcare.payload.request.UserEmailValidationRequest;
import com.system.vetcare.payload.response.AuthenticationResponse;
import com.system.vetcare.payload.response.UserEmailValidationResponse;
import com.system.vetcare.service.AuthenticationService;
import com.system.vetcare.service.JwtAuthenticationService;
import com.system.vetcare.service.JwtCookiesService;
import com.system.vetcare.service.UserService;
import com.system.vetcare.service.UsernameValidator;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(SECURITY_API_PATH)
public class AuthenticationController {

    private final UserService userService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final JwtCookiesService jwtCookiesService;
    private final UsernameValidator usernameValidator;
    private final AuthenticationService authenticationService;

    @PostMapping(USER_REGISTRATION)
    public ResponseEntity<AuthenticationResponse> performRegistration(
            @RequestBody UserRegistrationRequest userRegistrationRequest) {
        final UserPrincipal userPrincipal = userService.save(userRegistrationRequest);
        final JwtAuthenticationToken authenticationToken = jwtAuthenticationService.issueAuthenticationToken(userPrincipal);
        final HttpHeaders headers = jwtCookiesService.issueJwtCookies(authenticationToken);
        final AuthenticationResponse authenticationResponse = authenticationService.buildAuthenticationResponse(userPrincipal);
        return ResponseEntity
                .status(CREATED)
                .headers(headers)
                .body(authenticationResponse);
    }

    @PostMapping(USER_LOGIN)
    public ResponseEntity<AuthenticationResponse> performLogIn(@RequestBody AuthenticationRequest credential) {
        final UserPrincipal userPrincipal = authenticationService.authenticate(credential); 
        final JwtAuthenticationToken authenticationToken = jwtAuthenticationService.issueAuthenticationToken(userPrincipal);
        final HttpHeaders headers = jwtCookiesService.issueJwtCookies(authenticationToken);
        final AuthenticationResponse authenticationResponse = authenticationService.buildAuthenticationResponse(userPrincipal);
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(authenticationResponse);
    }
    
    @PostMapping(REFRESH_JWT_TOKEN)
    public ResponseEntity<Void> performRefreshToken(HttpServletRequest request) {
        final String jwtRefreshToken = jwtCookiesService.extractJwtToken(request.getCookies(), REFRESH_TOKEN);
        final JwtAuthenticationToken authenticationToken = jwtAuthenticationService.refreshAuthenticationToken(jwtRefreshToken);
        final HttpHeaders headers = jwtCookiesService.issueJwtCookies(authenticationToken);
        return ResponseEntity
                 .ok()
                 .headers(headers)
                 .build();
    }

    @PostMapping(USER_LOGOUT)
    public ResponseEntity<Void> performLogOut(HttpServletRequest request) {
        authenticationService.revokePrincipalAuthentication();
        final HttpHeaders headers = jwtCookiesService.revokeJwtCookies();
        final Cookie[] cookies = request.getCookies();
        final String jwtRefreshToken = jwtCookiesService.extractJwtToken(cookies, REFRESH_TOKEN);
        jwtAuthenticationService.revokeAuthenticationToken(jwtRefreshToken);
        return ResponseEntity
                .status(NO_CONTENT)
                .headers(headers)
                .build();
    }

    @PostMapping(VALIDATE_EMAIL)
    public ResponseEntity<UserEmailValidationResponse> validateUserEmail(
            @RequestBody UserEmailValidationRequest userEmailValidationRequest) {
        return ResponseEntity
                 .ok(usernameValidator.usernameIsAlreadyTaken(userEmailValidationRequest));
    }
    
}