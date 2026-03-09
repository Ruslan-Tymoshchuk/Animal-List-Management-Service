package com.system.vetcare.controller.filter;

import static com.system.vetcare.payload.JwtMarkers.*;
import static java.util.Objects.isNull;
import java.io.IOException;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import com.system.vetcare.service.JwtClaimsExtractor;
import com.system.vetcare.service.JwtCookiesService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RequestMatcher publicEndpointsMatcher;
    private final JwtClaimsExtractor jwtClaimsExtractor;
    private final JwtCookiesService jwtCookiesService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            final SecurityContext securityContext = SecurityContextHolder.getContext();
            if (isNull(securityContext.getAuthentication())) {
                final String jwtAccessToken = jwtCookiesService.extractJwtToken(request.getCookies(), ACCESS_TOKEN);
                final WebAuthenticationDetails webAuthenticationDetails = new WebAuthenticationDetailsSource()
                        .buildDetails(request);
                final Authentication authentication = buildAuthenticationToken(jwtAccessToken,
                        webAuthenticationDetails);
                securityContext.setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return publicEndpointsMatcher.matches(request);
    }
    
    private Authentication buildAuthenticationToken(String accessToken, WebAuthenticationDetails details) {
        final Claims claims = jwtClaimsExtractor.extractAccessTokenClaims(accessToken);
        final String email = jwtClaimsExtractor.extractEmail(claims);
        final Set<GrantedAuthority> authorities = jwtClaimsExtractor.extractAuthorities(claims);
        final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email,
                null, authorities);
        authenticationToken.setDetails(details);
        return authenticationToken;
    }

}