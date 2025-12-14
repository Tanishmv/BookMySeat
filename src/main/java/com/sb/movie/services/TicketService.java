package com.sb.movie.services;

import com.sb.movie.entities.Ticket;
import com.sb.movie.request.SeatLockRequest;
import com.sb.movie.request.TicketRequest;
import com.sb.movie.response.SeatLockResponse;
import com.sb.movie.response.TicketHistoryResponse;
import com.sb.movie.response.TicketResponse;

import java.util.List;

public interface TicketService {

    SeatLockResponse lockSeats(SeatLockRequest seatLockRequest);

    TicketResponse ticketBooking(TicketRequest ticketRequest);

    void releaseSeats(SeatLockRequest seatLockRequest);

    List<TicketHistoryResponse> getMyTickets(String userEmail);

    TicketHistoryResponse getTicketById(Integer ticketId);

    String cancelTicket(Integer ticketId, String userEmail);

}
