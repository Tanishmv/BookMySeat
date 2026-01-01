package com.sb.movie.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Add token to blacklist with expiration time
     */
    public void blacklistToken(String token, long expirationTimeMs) {
        blacklistedTokens.put(token, expirationTimeMs);
        log.info("Token blacklisted until: {}", expirationTimeMs);
    }

    /**
     * Check if token is blacklisted and not yet expired
     */
    public boolean isBlacklisted(String token) {
        Long expirationTime = blacklistedTokens.get(token);
        if (expirationTime == null) {
            return false;
        }

        // Check if token has expired
        if (System.currentTimeMillis() > expirationTime) {
            blacklistedTokens.remove(token);
            return false;
        }

        return true;
    }

    /**
     * Clean up expired tokens every 5 minutes
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
        log.debug("Cleaned up expired blacklisted tokens. Remaining: {}", blacklistedTokens.size());
    }
}
