package com.exam.service.auth;

import com.exam.model.auth.RefreshSessionEntity;
import com.exam.model.auth.UserEntity;
import com.exam.repository.auth.RefreshSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshSessionService {
    private final RefreshSessionRepository repository;

    @Value("${jwt.refresh-expiration-days:30}")
    private long refreshExpirationDays;
    private static final int MAX_SESSIONS = 5;

    @Transactional
    public RefreshSessionEntity createSession(UserEntity user, String fingerprint) {
        repository.deleteByUserIdAndFingerprint(user.getId(), fingerprint);
        checkAndCleanSessions(user.getId());

        RefreshSessionEntity session = new RefreshSessionEntity();
        session.setUser(user);
        session.setRefreshToken(UUID.randomUUID().toString());
        session.setFingerprint(fingerprint);
        session.setExpiresIn(Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS));

        return repository.save(session);
    }

    @Transactional
    public RefreshSessionEntity rotateSession(String oldToken, String fingerprint) {
        if (oldToken == null || oldToken.isBlank()) throw new IllegalArgumentException("Missing refresh token");
        RefreshSessionEntity session = repository.findByRefreshToken(oldToken).orElseThrow(() -> new IllegalArgumentException("Invalid session"));
        UserEntity user = session.getUser();
        repository.delete(session);

        if (Instant.now().isAfter(session.getExpiresIn())) throw new IllegalArgumentException("Session has expired");
        if (!session.getFingerprint().equals(fingerprint)) throw new IllegalArgumentException("Invalid session (fingerprint mismatch)");

        return createSession(user, fingerprint);
    }

    @Transactional
    public void revokeSession(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        repository.findByRefreshToken(token).ifPresent(repository::delete);
    }

    @Transactional
    public void revokeAllUserSessions(Long userId) {
        repository.deleteAllByUserId(userId);
    }

    private void checkAndCleanSessions(Long userId) {
        List<RefreshSessionEntity> sessions = repository.findAllByUserIdOrderByCreatedAtAsc(userId);
        if (sessions.size() >= MAX_SESSIONS) {
            for (int i = 0; i < sessions.size() - MAX_SESSIONS + 1; i++) {
                repository.delete(sessions.get(i));
            }
        }
    }
}
