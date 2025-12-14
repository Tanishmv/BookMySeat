package com.sb.movie.controllers;

import com.sb.movie.request.PasswordChangeRequest;
import com.sb.movie.request.UserRequest;
import com.sb.movie.response.UserProfileResponse;
import com.sb.movie.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user")
@Tag(name = "User Management", description = "APIs for user profile and account management")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/addNew")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<String> addNewUser(@RequestBody UserRequest userEntryDto) {
        try {
            String result = userService.addUser(userEntryDto);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Get profile details of the logged-in user")
    public ResponseEntity<Object> getCurrentUserProfile() {
        try {
            String userEmail = getCurrentUserEmail();
            UserProfileResponse profile = userService.getCurrentUserProfile(userEmail);
            return new ResponseEntity<>(profile, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/me")
    @Operation(summary = "Update user profile", description = "Update profile details of the logged-in user")
    public ResponseEntity<Object> updateUserProfile(@RequestBody UserRequest userRequest) {
        try {
            String userEmail = getCurrentUserEmail();
            UserProfileResponse profile = userService.updateUserProfile(userEmail, userRequest);
            return new ResponseEntity<>(profile, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/me/password")
    @Operation(summary = "Change password", description = "Change password for the logged-in user")
    public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        try {
            String userEmail = getCurrentUserEmail();
            String result = userService.changePassword(userEmail, passwordChangeRequest);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

}
