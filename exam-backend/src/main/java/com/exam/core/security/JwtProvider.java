package com.exam.core.security;

import com.exam.model.auth.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtProvider {
    private final SecretKey jwtAccessSecret;
    private final long accessTokenExpirationMinutes;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-expiration-minutes:30}") long expiration) {
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpirationMinutes = expiration;
    }

    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(jwtAccessSecret)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser().verifyWith(jwtAccessSecret).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.debug("Token expired", expEx);
        } catch (UnsupportedJwtException unsEx) {
            log.debug("Unsupported jwt", unsEx);
        } catch (MalformedJwtException mjEx) {
            log.debug("Malformed jwt", mjEx);
        } catch (SignatureException sEx) {
            log.debug("Invalid signature", sEx);
        } catch (Exception e) {
            log.debug("Invalid token", e);
        }
        return false;
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(jwtAccessSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Instant getIssuedAt(String token) {
        return Jwts.parser()
                .verifyWith(jwtAccessSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getIssuedAt()
                .toInstant();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtAccessSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

        return new UsernamePasswordAuthenticationToken(username, null, List.of(authority));
    }
}
