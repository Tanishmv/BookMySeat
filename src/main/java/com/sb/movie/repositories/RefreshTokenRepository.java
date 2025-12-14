package com.sb.movie.repositories;

import com.sb.movie.entities.RefreshToken;
import com.sb.movie.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < ?1")
    void deleteExpiredTokens(Instant now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = ?1")
    void revokeUserTokens(User user, Instant revokedAt);
}
