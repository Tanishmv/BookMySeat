package com.sb.movie;

import com.sb.movie.controllers.AuthRequest;
import com.sb.movie.enums.Gender;
import com.sb.movie.request.UserRequest;
import com.sb.movie.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        // Given - Register a user first with unique email
        String uniqueEmail = "test.user." + UUID.randomUUID() + "@example.com";
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Test User");
        userRequest.setAge(30);
        userRequest.setAddress("123 Test St");
        userRequest.setMobileNo("9999999999");
        userRequest.setEmailId(uniqueEmail);
        userRequest.setPassword("testpass123");
        userRequest.setGender(Gender.MALE);
        userRequest.setRoles("ROLE_USER");

        restTemplate.postForEntity("/user/addNew", userRequest, String.class);

        // When - Try to login
        AuthRequest authRequest = new AuthRequest(uniqueEmail, "testpass123");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                authRequest,
                AuthResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getRefreshToken()).isNotBlank();
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() {
        // Given
        AuthRequest authRequest = new AuthRequest("nonexistent@example.com", "wrongpassword");

        // Create a custom RestTemplate that doesn't throw on error status codes
        RestTemplate customRestTemplate = new RestTemplateBuilder()
                .rootUri(restTemplate.getRootUri())
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {
                        // Don't throw exception - let test handle the response
                    }
                })
                .build();

        // When
        ResponseEntity<String> response = customRestTemplate.postForEntity(
                "/api/auth/login",
                authRequest,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
