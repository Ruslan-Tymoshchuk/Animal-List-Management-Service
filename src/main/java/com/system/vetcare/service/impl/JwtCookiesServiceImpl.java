package com.system.vetcare.service.impl;

import static com.system.vetcare.payload.JwtMarkers.*;
import static com.system.vetcare.controller.constants.AuthenticationUrl.*;
import static  java.time.Duration.ZERO;
import static java.lang.String.format;
import static java.net.URLDecoder.decode;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.util.StringUtils.startsWithIgnoreCase;
import static java.util.Objects.nonNull;
import java.time.Duration;
import javax.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.system.vetcare.domain.JwtAuthenticationToken;
import com.system.vetcare.service.JwtCookiesService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtCookiesServiceImpl implements JwtCookiesService {

    public static final String JWT_FORMAT = "%s %s";
    public static final String EMPTY_STRING = "";
    public static final String LIMIT_THE_SCOPE = "None";
    public static final String COOKIES_NOT_PRESENT = "Cookies are not present";
    public static final String JWT_COOKIE_NOT_FOUND = "JWT token cookie with [name %s] was not found";

    @Value("${jwt.cookie.life.time}")
    private Duration jwtCookieLifeTime;

    @Override
    public HttpHeaders issueJwtCookies(JwtAuthenticationToken authenticationToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(SET_COOKIE,
                buildCookie(ACCESS_TOKEN,
                        format(JWT_FORMAT, BEARER_PREFIX, authenticationToken.accessToken()),
                        ABSOLUTE_API_PATH, jwtCookieLifeTime));
        headers.add(SET_COOKIE,
                buildCookie(REFRESH_TOKEN,
                        format(JWT_FORMAT, BEARER_PREFIX,
                                authenticationToken.refreshToken()),
                        SECURITY_API_PATH, jwtCookieLifeTime));
        return headers;
    }

    @Override
    public HttpHeaders revokeJwtCookies() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(SET_COOKIE, buildCookie(ACCESS_TOKEN, EMPTY_STRING, ABSOLUTE_API_PATH, ZERO));
        headers.add(SET_COOKIE, buildCookie(REFRESH_TOKEN, EMPTY_STRING, SECURITY_API_PATH, ZERO));
        return headers;
    }

    @Override
    public String extractJwtToken(Cookie[] cookies, String cookieName) {
        if (nonNull(cookies)) {
            for (Cookie cookie : cookies) {
                String cookieValue = cookie.getValue();
                if (cookie.getName().equals(cookieName) && StringUtils.hasText(cookieValue)
                        && startsWithIgnoreCase(cookieValue, BEARER_PREFIX)) {
                    return decode(cookieValue.substring(BEARER_PREFIX.length() + 1), UTF_8);
                }
            }
            throw new JwtException(format(JWT_COOKIE_NOT_FOUND, cookieName));
        } else {
            throw new JwtException(COOKIES_NOT_PRESENT);
        }
    }

    private String buildCookie(String name, String value, String path, Duration lifeTime) {
        return ResponseCookie
                 .from(name, encode(value, UTF_8))
                 .httpOnly(true)
                 .secure(true)
                 .sameSite(LIMIT_THE_SCOPE)
                 .path(path)
                 .maxAge(lifeTime)
                 .build()
                 .toString();
    }

}