package com.exam.web.controller.auth;

import com.exam.core.config.Constants;
import com.exam.core.log.NoLogging;
import com.exam.service.auth.AuthService;
import com.exam.web.dto.auth.AuthResponseDto;
import com.exam.web.dto.auth.LoginRequestDto;
import com.exam.web.dto.auth.RefreshRequestDto;
import com.exam.web.dto.auth.RegisterRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.API_URL + "/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${jwt.refresh-expiration-days:60}")
    private long refreshExpirationDays;

    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String AUTH_PATH = Constants.API_URL + "/auth";

    @NoLogging
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        AuthService.AuthResult result = authService.login(request.username(), request.password(), request.fingerprint());
        return buildAuthResponse(result);
    }

    @NoLogging
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        AuthService.AuthResult result = authService.register(request.username(), request.email(), request.password(), request.fingerprint());
        return buildAuthResponse(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                                   @Valid @RequestBody RefreshRequestDto request) {
        AuthService.AuthResult result = authService.refresh(refreshToken, request.fingerprint());
        return buildAuthResponse(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                       @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
        authService.logout(token, refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie("", 0))
                .build();
    }

    private ResponseEntity<AuthResponseDto> buildAuthResponse(AuthService.AuthResult result) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken(), refreshExpirationDays * 24 * 60 * 60))
                .body(result.responseDto());
    }

    private String buildRefreshCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .path(AUTH_PATH)
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build()
                .toString();
    }
}
