package com.sb.movie;

import com.sb.movie.controllers.AuthRequest;
import com.sb.movie.entities.Show;
import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Gender;
import com.sb.movie.enums.Genre;
import com.sb.movie.enums.Language;
import com.sb.movie.request.*;
import com.sb.movie.response.AuthResponse;
import com.sb.movie.response.ShowDetailsResponse;
import com.sb.movie.response.TheaterResponse;
import com.sb.movie.response.VenueResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ShowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String adminToken;
    private Integer venueId;
    private Integer theaterId;
    private Integer eventId;

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

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        // Create venue
        VenueRequest venueRequest = new VenueRequest();
        venueRequest.setName("Test Cinema Complex");
        venueRequest.setAddress("456 Movie Street");
        venueRequest.setCity("Mumbai");
        venueRequest.setDescription("Test venue for shows");

        HttpEntity<VenueRequest> venueReq = new HttpEntity<>(venueRequest, headers);
        restTemplate.exchange("/venue/addNew", HttpMethod.POST, venueReq, String.class);

        ResponseEntity<VenueResponse[]> venuesResponse = restTemplate.getForEntity(
                "/venue?city=Mumbai",
                VenueResponse[].class
        );
        venueId = venuesResponse.getBody()[venuesResponse.getBody().length - 1].getId();

        // Create theater
        TheaterRequest theaterRequest = new TheaterRequest();
        theaterRequest.setName("Screen 1");
        theaterRequest.setVenueId(venueId);
        theaterRequest.setNoOfSeatInRow(10);
        theaterRequest.setNoOfClassicSeat(60);
        theaterRequest.setNoOfPremiumSeat(40);

        HttpEntity<TheaterRequest> theaterReq = new HttpEntity<>(theaterRequest, headers);
        restTemplate.exchange("/theater/addNew", HttpMethod.POST, theaterReq, String.class);

        ResponseEntity<TheaterResponse[]> theatersResponse = restTemplate.getForEntity(
                "/theater",
                TheaterResponse[].class
        );
        theaterId = theatersResponse.getBody()[theatersResponse.getBody().length - 1].getId();

        // Create event
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName("Test Movie " + UUID.randomUUID());
        eventRequest.setEventType(EventType.MOVIE);
        eventRequest.setDuration(150);
        eventRequest.setRating(8.0);
        eventRequest.setReleaseDate(Date.valueOf("2024-01-01"));
        eventRequest.setGenre(Genre.ACTION);
        eventRequest.setLanguage(Language.ENGLISH);
        eventRequest.setDirector("Test Director");
        eventRequest.setDescription("Test movie for shows");

        HttpEntity<EventRequest> eventReq = new HttpEntity<>(eventRequest, headers);
        ResponseEntity<String> eventResponse = restTemplate.exchange(
                "/api/events",
                HttpMethod.POST,
                eventReq,
                String.class
        );

        // Extract event ID from response
        String eventResponseBody = eventResponse.getBody();
        // Parse the response to get all events and find the created one
        ResponseEntity<com.sb.movie.entities.Event[]> eventsResponse = restTemplate.getForEntity(
                "/api/events",
                com.sb.movie.entities.Event[].class
        );
        eventId = eventsResponse.getBody()[eventsResponse.getBody().length - 1].getId();
    }

    @Test
    void shouldPerformCompleteShowCRUD() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        // ========== CREATE (C) ==========
        ShowRequest createRequest = new ShowRequest();
        createRequest.setEventId(eventId);
        createRequest.setTheaterId(theaterId);
        createRequest.setShowDate(Date.valueOf(LocalDate.now().plusDays(7))); // Future date
        createRequest.setShowStartTime(Time.valueOf(LocalTime.of(18, 30, 0)));
        createRequest.setPriceOfClassicSeat(250);
        createRequest.setPriceOfPremiumSeat(450);

        HttpEntity<ShowRequest> createReq = new HttpEntity<>(createRequest, headers);
        ResponseEntity<Show> createResponse = restTemplate.exchange(
                "/api/shows/addNew",
                HttpMethod.POST,
                createReq,
                Show.class
        );

        // Verify CREATE
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getShowId()).isNotNull();
        // Note: showSeatList has @JsonIgnore, so it's not included in JSON response

        // ========== READ - Search Shows (R) ==========
        ResponseEntity<Show[]> searchResponse = restTemplate.getForEntity(
                "/api/shows?eventId=" + eventId,
                Show[].class
        );

        // Verify SEARCH
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody()).isNotNull();
        assertThat(searchResponse.getBody().length).isGreaterThan(0);

        // Get the created show ID
        Show createdShow = searchResponse.getBody()[0];
        Integer showId = createdShow.getShowId();

        // ========== READ - Get Show Details (R) ==========
        ResponseEntity<ShowDetailsResponse> getDetailsResponse = restTemplate.getForEntity(
                "/api/shows/" + showId,
                ShowDetailsResponse.class
        );

        // Verify GET DETAILS
        assertThat(getDetailsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getDetailsResponse.getBody()).isNotNull();
        assertThat(getDetailsResponse.getBody().getShowId()).isEqualTo(showId);
        assertThat(getDetailsResponse.getBody().getTotalSeats()).isEqualTo(100);
        assertThat(getDetailsResponse.getBody().getAvailableSeats()).isEqualTo(100);
        assertThat(getDetailsResponse.getBody().getTheater().getName()).isEqualTo("Screen 1");

        // ========== UPDATE (U) - Valid Future Date ==========
        ShowRequest updateRequest = new ShowRequest();
        updateRequest.setShowDate(Date.valueOf(LocalDate.now().plusDays(10))); // New future date
        updateRequest.setShowStartTime(Time.valueOf(LocalTime.of(20, 0, 0))); // New time

        HttpEntity<ShowRequest> updateReq = new HttpEntity<>(updateRequest, headers);
        ResponseEntity<Show> updateResponse = restTemplate.exchange(
                "/api/shows/" + showId,
                HttpMethod.PUT,
                updateReq,
                Show.class
        );

        // Verify UPDATE
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getTime()).isEqualTo(Time.valueOf(LocalTime.of(20, 0, 0)));

        // ========== DELETE (D) ==========
        HttpEntity<Void> deleteReq = new HttpEntity<>(headers);
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/api/shows/" + showId,
                HttpMethod.DELETE,
                deleteReq,
                String.class
        );

        // Verify DELETE
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody()).contains("deleted successfully");

        // Verify show is actually deleted
        ResponseEntity<String> getDeletedResponse = restTemplate.getForEntity(
                "/api/shows/" + showId,
                String.class
        );
        assertThat(getDeletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRejectUpdateWithPastDateTime() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        // Create a show first
        ShowRequest createRequest = new ShowRequest();
        createRequest.setEventId(eventId);
        createRequest.setTheaterId(theaterId);
        createRequest.setShowDate(Date.valueOf(LocalDate.now().plusDays(5)));
        createRequest.setShowStartTime(Time.valueOf(LocalTime.of(15, 0, 0)));
        createRequest.setPriceOfClassicSeat(200);
        createRequest.setPriceOfPremiumSeat(400);

        HttpEntity<ShowRequest> createReq = new HttpEntity<>(createRequest, headers);
        restTemplate.exchange("/api/shows/addNew", HttpMethod.POST, createReq, String.class);

        // Get the show ID
        ResponseEntity<Show[]> searchResponse = restTemplate.getForEntity(
                "/api/shows?eventId=" + eventId,
                Show[].class
        );
        Integer showId = searchResponse.getBody()[searchResponse.getBody().length - 1].getShowId();

        // Try to update with past date - should fail
        ShowRequest invalidUpdateRequest = new ShowRequest();
        invalidUpdateRequest.setShowDate(Date.valueOf(LocalDate.now().minusDays(1))); // Past date
        invalidUpdateRequest.setShowStartTime(Time.valueOf(LocalTime.of(10, 0, 0)));

        HttpEntity<ShowRequest> updateReq = new HttpEntity<>(invalidUpdateRequest, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "/api/shows/" + showId,
                HttpMethod.PUT,
                updateReq,
                String.class
        );

        // Verify validation failure
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(updateResponse.getBody()).contains("past date");
    }
}
