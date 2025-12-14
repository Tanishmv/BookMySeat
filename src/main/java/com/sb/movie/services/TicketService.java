package com.sb.movie.services;

import com.sb.movie.request.SeatLockRequest;
import com.sb.movie.request.TicketRequest;
import com.sb.movie.response.SeatLockResponse;
import com.sb.movie.response.TicketResponse;

public interface TicketService {

    SeatLockResponse lockSeats(SeatLockRequest seatLockRequest);

    TicketResponse ticketBooking(TicketRequest ticketRequest);

    void releaseSeats(SeatLockRequest seatLockRequest);

}
