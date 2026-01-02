package com.sb.movie.controllers;

import com.sb.movie.request.SeatLockRequest;
import com.sb.movie.request.TicketRequest;
import com.sb.movie.response.SeatLockResponse;
import com.sb.movie.response.TicketHistoryResponse;
import com.sb.movie.response.TicketResponse;
import com.sb.movie.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ticket")
@Tag(name = "Booking & Tickets", description = "Seat locking, ticket booking, and booking history management")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/lock-seats")
    @Operation(summary = "Lock seats temporarily", description = "Lock selected seats for 10 minutes to allow user to complete payment")
    public ResponseEntity<Object> lockSeats(@Valid @RequestBody SeatLockRequest seatLockRequest) {
        try {
            SeatLockResponse result = ticketService.lockSeats(seatLockRequest);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/book")
    @Operation(summary = "Confirm ticket booking", description = "Finalize booking and mark seats as BOOKED (works with or without prior seat lock)")
    public ResponseEntity<Object> ticketBooking(@Valid @RequestBody TicketRequest ticketRequest) {
        try {
            TicketResponse result = ticketService.ticketBooking(ticketRequest);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/release-seats")
    @Operation(summary = "Release locked seats", description = "Manually release locked seats before expiry")
    public ResponseEntity<Object> releaseSeats(@Valid @RequestBody SeatLockRequest seatLockRequest) {
        try {
            ticketService.releaseSeats(seatLockRequest);
            return new ResponseEntity<>("Seats released successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get my booking history", description = "Get all tickets for the current user")
    public ResponseEntity<Object> getMyTickets() {
        try {
            String userEmail = getCurrentUserEmail();
            List<TicketHistoryResponse> tickets = ticketService.getMyTickets(userEmail);
            return new ResponseEntity<>(tickets, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket details", description = "Get details of a specific ticket")
    public ResponseEntity<Object> getTicketById(@PathVariable Integer ticketId) {
        try {
            TicketHistoryResponse ticket = ticketService.getTicketById(ticketId);
            return new ResponseEntity<>(ticket, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{ticketId}")
    @Operation(summary = "Cancel ticket", description = "Cancel a ticket and get refund based on cancellation policy")
    public ResponseEntity<Object> cancelTicket(@PathVariable Integer ticketId) {
        try {
            String userEmail = getCurrentUserEmail();
            String result = ticketService.cancelTicket(ticketId, userEmail);
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
