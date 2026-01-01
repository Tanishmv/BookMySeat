package com.sb.movie;

import com.sb.movie.controllers.AuthRequest;
import com.sb.movie.entities.Event;
import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Gender;
import com.sb.movie.enums.Genre;
import com.sb.movie.enums.Language;
import com.sb.movie.request.EventRequest;
import com.sb.movie.request.UserRequest;
import com.sb.movie.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class EventIntegrationTest extends BaseIntegrationTest {

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
    void shouldPerformCompleteEventCRUD() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        // ========== CREATE (C) ==========
        EventRequest createRequest = new EventRequest();
        createRequest.setName("Avengers Endgame");
        createRequest.setEventType(EventType.MOVIE);
        createRequest.setDuration(181);
        createRequest.setRating(8.4);
        createRequest.setReleaseDate(Date.valueOf("2019-04-26"));
        createRequest.setGenre(Genre.ACTION);
        createRequest.setLanguage(Language.ENGLISH);
        createRequest.setDirector("Russo Brothers");
        createRequest.setPerformers("Robert Downey Jr., Chris Evans, Scarlett Johansson");
        createRequest.setDescription("The epic conclusion to the Infinity Saga");

        HttpEntity<EventRequest> createReq = new HttpEntity<>(createRequest, headers);
        ResponseEntity<Event> createResponse = restTemplate.exchange(
                "/api/events",
                HttpMethod.POST,
                createReq,
                Event.class
        );

        // Verify CREATE
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getId()).isNotNull();
        assertThat(createResponse.getBody().getName()).isEqualTo("Avengers Endgame");

        // ========== READ - Get All (R) ==========
        ResponseEntity<Event[]> getAllResponse = restTemplate.getForEntity(
                "/api/events",
                Event[].class
        );

        // Verify READ ALL
        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody()).isNotNull();
        assertThat(getAllResponse.getBody().length).isGreaterThan(0);

        // Get the created event ID
        Event createdEvent = getAllResponse.getBody()[getAllResponse.getBody().length - 1];
        Integer eventId = createdEvent.getId();
        assertThat(createdEvent.getName()).isEqualTo("Avengers Endgame");

        // ========== READ - Get By ID (R) ==========
        ResponseEntity<Event> getByIdResponse = restTemplate.getForEntity(
                "/api/events/" + eventId,
                Event.class
        );

        // Verify READ BY ID
        assertThat(getByIdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getByIdResponse.getBody()).isNotNull();
        assertThat(getByIdResponse.getBody().getName()).isEqualTo("Avengers Endgame");
        assertThat(getByIdResponse.getBody().getEventType()).isEqualTo(EventType.MOVIE);
        assertThat(getByIdResponse.getBody().getGenre()).isEqualTo(Genre.ACTION);
        assertThat(getByIdResponse.getBody().getRating()).isEqualTo(8.4);

        // ========== UPDATE (U) ==========
        EventRequest updateRequest = new EventRequest();
        updateRequest.setName("Avengers Endgame - Remastered");
        updateRequest.setEventType(EventType.MOVIE);
        updateRequest.setDuration(181);
        updateRequest.setRating(8.8); // Updated rating
        updateRequest.setReleaseDate(Date.valueOf("2024-04-26")); // Re-release date
        updateRequest.setGenre(Genre.ACTION);
        updateRequest.setLanguage(Language.ENGLISH);
        updateRequest.setDirector("Russo Brothers");
        updateRequest.setPerformers("Robert Downey Jr., Chris Evans, Scarlett Johansson");
        updateRequest.setDescription("Remastered version with enhanced visuals");

        HttpEntity<EventRequest> updateReq = new HttpEntity<>(updateRequest, headers);
        ResponseEntity<Event> updateResponse = restTemplate.exchange(
                "/api/events/" + eventId,
                HttpMethod.PUT,
                updateReq,
                Event.class
        );

        // Verify UPDATE
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getName()).isEqualTo("Avengers Endgame - Remastered");

        // Verify update took effect
        ResponseEntity<Event> getUpdatedResponse = restTemplate.getForEntity(
                "/api/events/" + eventId,
                Event.class
        );
        assertThat(getUpdatedResponse.getBody().getName()).isEqualTo("Avengers Endgame - Remastered");
        assertThat(getUpdatedResponse.getBody().getRating()).isEqualTo(8.8);

        // ========== DELETE (D) ==========
        HttpEntity<Void> deleteReq = new HttpEntity<>(headers);
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/api/events/" + eventId,
                HttpMethod.DELETE,
                deleteReq,
                String.class
        );

        // Verify DELETE
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody()).contains("deleted successfully");

        // Verify event is actually deleted
        ResponseEntity<String> getDeletedResponse = restTemplate.getForEntity(
                "/api/events/" + eventId,
                String.class
        );
        assertThat(getDeletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
