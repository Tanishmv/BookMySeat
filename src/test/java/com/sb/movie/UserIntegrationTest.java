package com.sb.movie;

import com.sb.movie.enums.Gender;
import com.sb.movie.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldRegisterNewUser() {
        // Given - Use unique email for each test run
        String uniqueEmail = "john.doe." + UUID.randomUUID() + "@test.com";
        UserRequest userRequest = new UserRequest();
        userRequest.setName("John Doe");
        userRequest.setAge(25);
        userRequest.setAddress("123 Main St");
        userRequest.setMobileNo("1234567890");
        userRequest.setEmailId(uniqueEmail);
        userRequest.setPassword("password123");
        userRequest.setGender(Gender.MALE);
        userRequest.setRoles("ROLE_USER");

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/user/addNew",
                userRequest,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("User Saved Successfully");
    }

    @Test
    void shouldNotRegisterUserWithDuplicateEmail() {
        // Given - Register first user with unique email
        String uniqueEmail = "jane.doe." + UUID.randomUUID() + "@test.com";
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Jane Doe");
        userRequest.setAge(28);
        userRequest.setAddress("456 Oak Ave");
        userRequest.setMobileNo("9876543210");
        userRequest.setEmailId(uniqueEmail);
        userRequest.setPassword("password456");
        userRequest.setGender(Gender.FEMALE);
        userRequest.setRoles("ROLE_USER");

        restTemplate.postForEntity("/user/addNew", userRequest, String.class);

        // When - Try to register with same email
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/user/addNew",
                userRequest,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
