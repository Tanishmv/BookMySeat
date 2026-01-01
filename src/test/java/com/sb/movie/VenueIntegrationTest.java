package com.sb.movie;

import com.sb.movie.controllers.AuthRequest;
import com.sb.movie.enums.Gender;
import com.sb.movie.request.UserRequest;
import com.sb.movie.request.VenueRequest;
import com.sb.movie.response.AuthResponse;
import com.sb.movie.response.VenueResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class VenueIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // Create admin user and login
        String adminEmail = "admin." + UUID.randomUUID() + "@test.com";
        UserRequest adminUser = new UserRequest();
        adminUser.setName("Admin User");
        adminUser.setAge(30);
        adminUser.setAddress("Admin Street");
        adminUser.setMobileNo("9999999999");
        adminUser.setEmailId(adminEmail);
        adminUser.setPassword("admin123");
        adminUser.setGender(Gender.MALE);
        adminUser.setRoles("ROLE_ADMIN");

        restTemplate.postForEntity("/user/addNew", adminUser, String.class);

        AuthRequest authRequest = new AuthRequest(adminEmail, "admin123");
        ResponseEntity<AuthResponse> authResponse = restTemplate.postForEntity(
                "/api/auth/login",
                authRequest,
                AuthResponse.class
        );
        adminToken = authResponse.getBody().getAccessToken();
    }

    @Test
    void shouldPerformCompleteVenueCRUD() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        // ========== CREATE (C) ==========
        VenueRequest createRequest = new VenueRequest();
        createRequest.setName("Phoenix Marketcity Mall");
        createRequest.setAddress("142, Lal Bahadur Shastri Rd, Kurla West");
        createRequest.setCity("Mumbai");
        createRequest.setDescription("Premium shopping and entertainment destination");

        HttpEntity<VenueRequest> createReq = new HttpEntity<>(createRequest, headers);
        ResponseEntity<com.sb.movie.response.VenueResponse> createResponse = restTemplate.exchange(
                "/venue/addNew",
                HttpMethod.POST,
                createReq,
                com.sb.movie.response.VenueResponse.class
        );

        // Verify CREATE
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getId()).isNotNull();
        assertThat(createResponse.getBody().getName()).isEqualTo("Phoenix Marketcity Mall");

        // ========== READ - Get All (R) ==========
        ResponseEntity<VenueResponse[]> getAllResponse = restTemplate.getForEntity(
                "/venue",
                VenueResponse[].class
        );

        // Verify READ ALL
        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody()).isNotNull();
        assertThat(getAllResponse.getBody().length).isGreaterThan(0);

        // Get the created venue ID
        VenueResponse createdVenue = getAllResponse.getBody()[getAllResponse.getBody().length - 1];
        Integer venueId = createdVenue.getId();
        assertThat(createdVenue.getName()).isEqualTo("Phoenix Marketcity Mall");
        assertThat(createdVenue.getCity()).isEqualTo("Mumbai");

        // ========== READ - Get By ID (R) ==========
        ResponseEntity<VenueResponse> getByIdResponse = restTemplate.getForEntity(
                "/venue/" + venueId,
                VenueResponse.class
        );

        // Verify READ BY ID
        assertThat(getByIdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getByIdResponse.getBody()).isNotNull();
        assertThat(getByIdResponse.getBody().getName()).isEqualTo("Phoenix Marketcity Mall");
        assertThat(getByIdResponse.getBody().getAddress()).isEqualTo("142, Lal Bahadur Shastri Rd, Kurla West");

        // ========== READ - Get By City (R) ==========
        ResponseEntity<VenueResponse[]> getByCityResponse = restTemplate.getForEntity(
                "/venue?city=Mumbai",
                VenueResponse[].class
        );

        // Verify READ BY CITY
        assertThat(getByCityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getByCityResponse.getBody()).isNotNull();
        assertThat(getByCityResponse.getBody().length).isGreaterThan(0);

        // ========== UPDATE (U) ==========
        VenueRequest updateRequest = new VenueRequest();
        updateRequest.setName("Phoenix Marketcity - Updated");
        updateRequest.setAddress("142, Lal Bahadur Shastri Rd, Kurla West, Mumbai");
        updateRequest.setCity("Mumbai");
        updateRequest.setDescription("Updated description - Premium mall with world-class facilities");

        HttpEntity<VenueRequest> updateReq = new HttpEntity<>(updateRequest, headers);
        ResponseEntity<com.sb.movie.response.VenueResponse> updateResponse = restTemplate.exchange(
                "/venue/" + venueId,
                HttpMethod.PUT,
                updateReq,
                com.sb.movie.response.VenueResponse.class
        );

        // Verify UPDATE
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getName()).isEqualTo("Phoenix Marketcity - Updated");

        // Verify update took effect
        ResponseEntity<VenueResponse> getUpdatedResponse = restTemplate.getForEntity(
                "/venue/" + venueId,
                VenueResponse.class
        );
        assertThat(getUpdatedResponse.getBody().getName()).isEqualTo("Phoenix Marketcity - Updated");
        assertThat(getUpdatedResponse.getBody().getDescription()).contains("world-class facilities");

        // ========== DELETE (D) ==========
        HttpEntity<Void> deleteReq = new HttpEntity<>(headers);
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/venue/" + venueId,
                HttpMethod.DELETE,
                deleteReq,
                String.class
        );

        // Verify DELETE
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody()).contains("deleted successfully");

        // Verify venue is actually deleted
        ResponseEntity<String> getDeletedResponse = restTemplate.getForEntity(
                "/venue/" + venueId,
                String.class
        );
        assertThat(getDeletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
