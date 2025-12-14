package com.sb.movie.services;

import com.sb.movie.entities.RefreshToken;
import com.sb.movie.entities.User;
import com.sb.movie.repositories.RefreshTokenRepository;
import com.sb.movie.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days in milliseconds
    private Long refreshTokenDurationMs;

    /**
     * Creates a new refresh token for the given username
     */
    @Transactional
    public RefreshToken createRefreshToken(String username) {
        log.info("Creating refresh token for user: {}", username);

        User user = userRepository.findByEmailId(username)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + username));

        // Revoke any existing refresh tokens for this user
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            log.debug("Revoking existing refresh token for user: {}", username);
            refreshTokenRepository.revokeUserTokens(user, Instant.now());
        }

        // Create new refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .createdAt(Instant.now())
                .revoked(false)
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Successfully created refresh token for user: {}", username);

        return refreshToken;
    }

    /**
     * Verifies the refresh token is valid (exists, not expired, not revoked)
     */
    public RefreshToken verifyRefreshToken(String token) {
        log.debug("Verifying refresh token: {}", token);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            log.warn("Refresh token has expired: {}", token);
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired. Please login again");
        }

        if (refreshToken.isRevoked()) {
            log.warn("Refresh token has been revoked: {}", token);
            throw new RuntimeException("Refresh token has been revoked. Please login again");
        }

        log.debug("Refresh token verified successfully");
        return refreshToken;
    }

    /**
     * Revokes a specific refresh token
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        log.info("Revoking refresh token: {}", token);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token revoked successfully");
    }

    /**
     * Revokes all refresh tokens for a user (useful for logout from all devices)
     */
    @Transactional
    public void revokeAllUserTokens(String username) {
        log.info("Revoking all refresh tokens for user: {}", username);

        User user = userRepository.findByEmailId(username)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + username));

        refreshTokenRepository.revokeUserTokens(user, Instant.now());
        log.info("All refresh tokens revoked for user: {}", username);
    }

    /**
     * Scheduled task to clean up expired tokens
     * Runs every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Expired refresh tokens cleanup completed");
    }
}
