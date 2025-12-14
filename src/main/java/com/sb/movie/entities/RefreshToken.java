package com.sb.movie.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant revokedAt;

    @Column(nullable = false)
    private Boolean revoked = false;

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }

    public boolean isRevoked() {
        return revoked != null && revoked;
    }

    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }
}
