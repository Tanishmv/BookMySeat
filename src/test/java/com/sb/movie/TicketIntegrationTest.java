package com.sb.movie;

import com.sb.movie.controllers.AuthRequest;
import com.sb.movie.entities.Show;
import com.sb.movie.entities.Theater;
import com.sb.movie.enums.EventType;
import com.sb.movie.enums.Gender;
import com.sb.movie.enums.Language;
import com.sb.movie.request.*;
import com.sb.movie.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class TicketIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String userToken;
    private String adminToken;
    private Integer userId;
    private Integer showId;

    @BeforeEach
    void setUp() {
        // 1. Create and login regular user
        String userEmail = "user." + UUID.randomUUID() + "@test.com";
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Test User");
        userRequest.setAge(25);
        userRequest.setAddress("Test Address");
        userRequest.setMobileNo("9876543210");
        userRequest.setEmailId(userEmail);
        userRequest.setPassword("password123");
        userRequest.setGender(Gender.MALE);
        userRequest.setRoles("ROLE_USER");

        restTemplate.postForEntity("/user/addNew", userRequest, String.class);

        AuthRequest authRequest = new AuthRequest(userEmail, "password123");
        ResponseEntity<AuthResponse> authResponse = restTemplate.postForEntity(
                "/api/auth/login",
                authRequest,
                AuthResponse.class
        );
        userToken = authResponse.getBody().getAccessToken();
        userId = 1; // Simplified

        // 2. Create admin user
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

        AuthRequest adminAuthRequest = new AuthRequest(adminEmail, "admin123");
        ResponseEntity<AuthResponse> adminAuthResponse = restTemplate.postForEntity(
                "/api/auth/login",
                adminAuthRequest,
                AuthResponse.class
        );
        adminToken = adminAuthResponse.getBody().getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        // 3. Create Event
        EventRequest eventRequest = new EventRequest();
        eventRequest.setName("Test Movie " + UUID.randomUUID());
        eventRequest.setEventType(EventType.MOVIE);
        eventRequest.setCity("Bangalore");
        eventRequest.setLanguage(Language.ENGLISH);

        HttpEntity<EventRequest> eventReq = new HttpEntity<>(eventRequest, headers);
        restTemplate.exchange("/api/events", HttpMethod.POST, eventReq, String.class);

        // Get the created event ID
        ResponseEntity<com.sb.movie.entities.Event[]> eventsResponse = restTemplate.getForEntity(
                "/api/events",
                com.sb.movie.entities.Event[].class
        );
        Integer eventId = eventsResponse.getBody()[eventsResponse.getBody().length - 1].getId();

        // 4. Create Theater
        TheaterRequest theaterRequest = new TheaterRequest();
        theaterRequest.setName("PVR Cinemas " + UUID.randomUUID());
        theaterRequest.setAddress("MG Road, Bangalore");
        theaterRequest.setCity("Bangalore");

        restTemplate.postForEntity("/theater/addNew", theaterRequest, String.class);

        // Get the created theater ID
        ResponseEntity<Theater[]> theatersResponse = restTemplate.getForEntity(
                "/theater",
                Theater[].class
        );
        Integer theaterId = theatersResponse.getBody()[theatersResponse.getBody().length - 1].getId();

        // 5. Add seats to theater
        TheaterSeatRequest seatRequest = new TheaterSeatRequest();
        seatRequest.setTheaterId(theaterId);
        seatRequest.setNoOfSeatInRow(10);
        seatRequest.setNoOfPremiumSeat(20);
        seatRequest.setNoOfClassicSeat(30);

        restTemplate.postForEntity("/theater/addTheaterSeat", seatRequest, String.class);

        // 6. Create Show
        ShowRequest showRequest = new ShowRequest();
        showRequest.setShowStartTime(Time.valueOf("18:00:00"));
        showRequest.setShowDate(Date.valueOf("2025-12-25"));
        showRequest.setEventId(eventId);
        showRequest.setTheaterId(theaterId);

        restTemplate.postForEntity("/api/shows/addNew", showRequest, String.class);

        // Get the created show ID
        ResponseEntity<Show[]> showsResponse = restTemplate.getForEntity(
                "/api/shows",
                Show[].class
        );
        showId = showsResponse.getBody()[showsResponse.getBody().length - 1].getShowId();

        // 7. Associate seats with show
        ShowSeatRequest showSeatRequest = new ShowSeatRequest();
        showSeatRequest.setShowId(showId);
        showSeatRequest.setPriceOfPremiumSeat(300);
        showSeatRequest.setPriceOfClassicSeat(200);

        restTemplate.postForEntity("/api/shows/associateSeats", showSeatRequest, String.class);
    }

    @Test
    void shouldBookTicketSuccessfully() {
        // Given - Lock seats first
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        SeatLockRequest lockRequest = new SeatLockRequest();
        lockRequest.setShowId(showId);
        lockRequest.setUserId(userId);
        lockRequest.setRequestSeats(Arrays.asList("1A", "1B"));

        HttpEntity<SeatLockRequest> lockReq = new HttpEntity<>(lockRequest, headers);
        ResponseEntity<String> lockResponse = restTemplate.exchange(
                "/ticket/lock-seats",
                HttpMethod.POST,
                lockReq,
                String.class
        );

        // Verify lock was successful
        assertThat(lockResponse.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CREATED);

        // Book the locked seats
        TicketRequest ticketRequest = new TicketRequest();
        ticketRequest.setShowId(showId);
        ticketRequest.setUserId(userId);
        ticketRequest.setRequestSeats(Arrays.asList("1A", "1B"));

        HttpEntity<TicketRequest> request = new HttpEntity<>(ticketRequest, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/ticket/book",
                HttpMethod.POST,
                request,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldPreventDoubleBooking() {
        // Given - Book seats first
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        SeatLockRequest lockRequest = new SeatLockRequest();
        lockRequest.setShowId(showId);
        lockRequest.setUserId(userId);
        lockRequest.setRequestSeats(Arrays.asList("2A", "2B"));

        HttpEntity<SeatLockRequest> lockReq = new HttpEntity<>(lockRequest, headers);
        restTemplate.exchange("/ticket/lock-seats", HttpMethod.POST, lockReq, String.class);

        TicketRequest ticketRequest = new TicketRequest();
        ticketRequest.setShowId(showId);
        ticketRequest.setUserId(userId);
        ticketRequest.setRequestSeats(Arrays.asList("2A", "2B"));

        HttpEntity<TicketRequest> bookReq = new HttpEntity<>(ticketRequest, headers);
        restTemplate.exchange("/ticket/book", HttpMethod.POST, bookReq, String.class);

        // Try to book same seats again
        SeatLockRequest secondLockRequest = new SeatLockRequest();
        secondLockRequest.setShowId(showId);
        secondLockRequest.setUserId(userId);
        secondLockRequest.setRequestSeats(Arrays.asList("2A", "2B"));

        HttpEntity<SeatLockRequest> secondLockReq = new HttpEntity<>(secondLockRequest, headers);

        // When - Attempt to lock already booked seats
        ResponseEntity<String> response = restTemplate.exchange(
                "/ticket/lock-seats",
                HttpMethod.POST,
                secondLockReq,
                String.class
        );

        // Then - Should fail with BAD_REQUEST
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        // Check that error message mentions seat unavailability
        assertThat(response.getBody().toLowerCase()).containsAnyOf("not available", "already", "booked", "unavailable");
    }
}
