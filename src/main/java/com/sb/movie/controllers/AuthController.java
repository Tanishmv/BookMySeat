package com.sb.movie.controllers;

import com.sb.movie.entities.RefreshToken;
import com.sb.movie.request.RefreshTokenRequest;
import com.sb.movie.response.AuthResponse;
import com.sb.movie.security.JWTService;
import com.sb.movie.services.RefreshTokenService;
import com.sb.movie.services.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication and token management")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return access and refresh tokens")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            log.info("Login attempt for user: {}", authRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                // Generate access token
                String accessToken = jwtService.generateToken(authRequest.getUsername());

                // Generate refresh token
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequest.getUsername());

                // Get expiration time
                Long expiresIn = jwtService.getAccessTokenExpiration();

                log.info("User {} authenticated successfully", authRequest.getUsername());

                AuthResponse response = new AuthResponse(accessToken, refreshToken.getToken(), expiresIn);
                return ResponseEntity.ok(response);
            } else {
                log.warn("Authentication failed for user: {}", authRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid username or password");
            }
        } catch (AuthenticationException e) {
            log.error("Authentication error for user {}: {}", authRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        } catch (Exception e) {
            log.error("Unexpected error during authentication: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during authentication");
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            log.info("Refresh token request received");

            // Verify refresh token
            RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(
                    refreshTokenRequest.getRefreshToken()
            );

            // Generate new access token
            String username = refreshToken.getUser().getEmailId();
            String newAccessToken = jwtService.generateToken(username);

            // Get expiration time
            Long expiresIn = jwtService.getAccessTokenExpiration();

            log.info("New access token generated for user: {}", username);

            AuthResponse response = new AuthResponse(
                    newAccessToken,
                    refreshToken.getToken(),
                    expiresIn
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Refresh token error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during token refresh");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Revoke access token and refresh token")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            log.info("Logout request received");

            // Extract access token from Authorization header
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);

                // Blacklist the access token
                long expirationTime = jwtService.getExpirationTimeMs(accessToken);
                tokenBlacklistService.blacklistToken(accessToken, expirationTime);
                log.info("Access token blacklisted");
            }

            // Revoke refresh token
            refreshTokenService.revokeRefreshToken(refreshTokenRequest.getRefreshToken());

            log.info("User logged out successfully");
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Logout failed: " + e.getMessage());
        }
    }
}
