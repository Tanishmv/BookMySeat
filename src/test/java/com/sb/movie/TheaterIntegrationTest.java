package com.sb.movie;

import com.sb.movie.controllers.AuthRequest;
import com.sb.movie.enums.Gender;
import com.sb.movie.request.TheaterRequest;
import com.sb.movie.request.TheaterUpdateRequest;
import com.sb.movie.request.UserRequest;
import com.sb.movie.request.VenueRequest;
import com.sb.movie.response.AuthResponse;
import com.sb.movie.response.TheaterResponse;
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
class TheaterIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken;
    private Integer venueId;

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

        // Create a venue for theater tests
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        VenueRequest venueRequest = new VenueRequest();
        venueRequest.setName("Test Mall");
        venueRequest.setAddress("123 Test Street, Test Area");
        venueRequest.setCity("Mumbai");
        venueRequest.setDescription("Test venue for integration tests");

        HttpEntity<VenueRequest> venueReq = new HttpEntity<>(venueRequest, headers);
        restTemplate.exchange("/venue/addNew", HttpMethod.POST, venueReq, String.class);

        // Get the created venue
        ResponseEntity<VenueResponse[]> venuesResponse = restTemplate.getForEntity(
                "/venue?city=Mumbai",
                VenueResponse[].class
        );
        venueId = venuesResponse.getBody()[venuesResponse.getBody().length - 1].getId();
    }

    @Test
    void shouldPerformCompleteTheaterCRUD() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        // ========== CREATE (C) ==========
        TheaterRequest createRequest = new TheaterRequest();
        createRequest.setName("IMAX Screen 1");
        createRequest.setVenueId(venueId);
        createRequest.setNoOfSeatInRow(10);
        createRequest.setNoOfClassicSeat(50);
        createRequest.setNoOfPremiumSeat(30);

        HttpEntity<TheaterRequest> createReq = new HttpEntity<>(createRequest, headers);
        ResponseEntity<String> createResponse = restTemplate.exchange(
                "/theater/addNew",
                HttpMethod.POST,
                createReq,
                String.class
        );

        // Verify CREATE
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).contains("saved successfully");
        assertThat(createResponse.getBody()).contains("80 seats");

        // ========== READ - Get All (R) ==========
        ResponseEntity<TheaterResponse[]> getAllResponse = restTemplate.getForEntity(
                "/theater",
                TheaterResponse[].class
        );

        // Verify READ ALL
        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody()).isNotNull();
        assertThat(getAllResponse.getBody().length).isGreaterThan(0);

        // Get the created theater ID
        TheaterResponse createdTheater = getAllResponse.getBody()[getAllResponse.getBody().length - 1];
        Integer theaterId = createdTheater.getId();
        assertThat(createdTheater.getName()).isEqualTo("IMAX Screen 1");
        assertThat(createdTheater.getTotalSeats()).isEqualTo(80);
        assertThat(createdTheater.getTotalClassicSeats()).isEqualTo(50);
        assertThat(createdTheater.getTotalPremiumSeats()).isEqualTo(30);

        // ========== READ - Get By ID (R) ==========
        ResponseEntity<TheaterResponse> getByIdResponse = restTemplate.getForEntity(
                "/theater/" + theaterId,
                TheaterResponse.class
        );

        // Verify READ BY ID
        assertThat(getByIdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getByIdResponse.getBody()).isNotNull();
        assertThat(getByIdResponse.getBody().getName()).isEqualTo("IMAX Screen 1");
        assertThat(getByIdResponse.getBody().getVenue()).isNotNull();
        assertThat(getByIdResponse.getBody().getVenue().getName()).isEqualTo("Test Mall");

        // ========== READ - Get By City (R) ==========
        ResponseEntity<TheaterResponse[]> getByCityResponse = restTemplate.getForEntity(
                "/theater?city=Mumbai",
                TheaterResponse[].class
        );

        // Verify READ BY CITY
        assertThat(getByCityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getByCityResponse.getBody()).isNotNull();
        assertThat(getByCityResponse.getBody().length).isGreaterThan(0);

        // ========== UPDATE (U) ==========
        // Using TheaterUpdateRequest which only accepts name
        TheaterUpdateRequest updateRequest = new TheaterUpdateRequest();
        updateRequest.setName("IMAX Screen 1 - Premium");

        HttpEntity<TheaterUpdateRequest> updateReq = new HttpEntity<>(updateRequest, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "/theater/" + theaterId,
                HttpMethod.PUT,
                updateReq,
                String.class
        );

        // Verify UPDATE
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).contains("updated successfully");

        // Verify update took effect (only name should change, not venue or seats)
        ResponseEntity<TheaterResponse> getUpdatedResponse = restTemplate.getForEntity(
                "/theater/" + theaterId,
                TheaterResponse.class
        );
        assertThat(getUpdatedResponse.getBody().getName()).isEqualTo("IMAX Screen 1 - Premium");
        assertThat(getUpdatedResponse.getBody().getTotalSeats()).isEqualTo(80); // Seats unchanged
        assertThat(getUpdatedResponse.getBody().getVenue().getId()).isEqualTo(venueId); // Venue unchanged

        // ========== DELETE (D) ==========
        HttpEntity<Void> deleteReq = new HttpEntity<>(headers);
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/theater/" + theaterId,
                HttpMethod.DELETE,
                deleteReq,
                String.class
        );

        // Verify DELETE
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody()).contains("deleted successfully");

        // Verify theater is actually deleted
        ResponseEntity<String> getDeletedResponse = restTemplate.getForEntity(
                "/theater/" + theaterId,
                String.class
        );
        assertThat(getDeletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRejectTheaterUpdateWithInvalidBody() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        // Create a theater first
        TheaterRequest createRequest = new TheaterRequest();
        createRequest.setName("Test Theater");
        createRequest.setVenueId(venueId);
        createRequest.setNoOfSeatInRow(10);
        createRequest.setNoOfClassicSeat(50);
        createRequest.setNoOfPremiumSeat(30);

        HttpEntity<TheaterRequest> createReq = new HttpEntity<>(createRequest, headers);
        restTemplate.exchange("/theater/addNew", HttpMethod.POST, createReq, String.class);

        // Get the theater ID
        ResponseEntity<TheaterResponse[]> getAllResponse = restTemplate.getForEntity(
                "/theater",
                TheaterResponse[].class
        );
        Integer theaterId = getAllResponse.getBody()[getAllResponse.getBody().length - 1].getId();

        // Try to update with empty name - should fail validation
        TheaterUpdateRequest invalidRequest = new TheaterUpdateRequest();
        invalidRequest.setName(""); // Blank name should fail @NotBlank validation

        HttpEntity<TheaterUpdateRequest> updateReq = new HttpEntity<>(invalidRequest, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "/theater/" + theaterId,
                HttpMethod.PUT,
                updateReq,
                String.class
        );

        // Verify validation failure
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
