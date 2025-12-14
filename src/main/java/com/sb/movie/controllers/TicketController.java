package com.sb.movie.controllers;

import com.sb.movie.request.SeatLockRequest;
import com.sb.movie.request.TicketRequest;
import com.sb.movie.response.SeatLockResponse;
import com.sb.movie.response.TicketResponse;
import com.sb.movie.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticket")
@Tag(name = "Ticket", description = "Ticket booking and seat management APIs")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/lock-seats")
    @Operation(summary = "Lock seats", description = "Lock selected seats for 10 minutes before booking")
    public ResponseEntity<Object> lockSeats(@RequestBody SeatLockRequest seatLockRequest) {
        try {
            SeatLockResponse result = ticketService.lockSeats(seatLockRequest);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/book")
    @Operation(summary = "Book tickets", description = "Confirm booking for locked seats")
    public ResponseEntity<Object> ticketBooking(@RequestBody TicketRequest ticketRequest) {
        try {
            TicketResponse result = ticketService.ticketBooking(ticketRequest);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/release-seats")
    @Operation(summary = "Release locked seats", description = "Manually release locked seats before expiry")
    public ResponseEntity<Object> releaseSeats(@RequestBody SeatLockRequest seatLockRequest) {
        try {
            ticketService.releaseSeats(seatLockRequest);
            return new ResponseEntity<>("Seats released successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
