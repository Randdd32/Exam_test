package com.exam.service.auth;

import com.exam.core.security.JwtProvider;
import com.exam.model.auth.RefreshSessionEntity;
import com.exam.model.auth.UserEntity;
import com.exam.model.enums.UserRole;
import com.exam.web.dto.auth.AuthResponseDto;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final RefreshSessionService sessionService;
    private final JwtProvider jwtProvider;
    private final Cache<@NonNull String, Boolean> blacklistedTokensCache;

    @Transactional
    public AuthResult login(String username, String password, String fingerprint) {
        UserEntity user = userService.validateAndGetUser(username, password);
        RefreshSessionEntity session = sessionService.createSession(user, fingerprint);
        return new AuthResult(new AuthResponseDto(jwtProvider.generateAccessToken(user), user.getUsername(), user.getEmail(), user.getRole()),
                session.getRefreshToken());
    }

    @Transactional
    public AuthResult register(String username, String email, String password, String fingerprint) {
        UserEntity user = userService.createUser(username, email, password, UserRole.USER);
        RefreshSessionEntity session = sessionService.createSession(user, fingerprint);
        return new AuthResult(new AuthResponseDto(jwtProvider.generateAccessToken(user), user.getUsername(), user.getEmail(), user.getRole()),
                session.getRefreshToken());
    }

    @Transactional
    public AuthResult refresh(String oldToken, String fingerprint) {
        RefreshSessionEntity newSession = sessionService.rotateSession(oldToken, fingerprint);
        UserEntity user = newSession.getUser();
        return new AuthResult(new AuthResponseDto(jwtProvider.generateAccessToken(user), user.getUsername(), user.getEmail(), user.getRole()),
                newSession.getRefreshToken());
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null) {
            blacklistedTokensCache.put(accessToken, true);
        }
        sessionService.revokeSession(refreshToken);
    }

    public record AuthResult(AuthResponseDto responseDto, String refreshToken) {}
}
