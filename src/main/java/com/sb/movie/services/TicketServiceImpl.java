package com.sb.movie.services;

import com.sb.movie.converter.TicketConvertor;
import com.sb.movie.entities.Show;
import com.sb.movie.entities.ShowSeat;
import com.sb.movie.entities.Ticket;
import com.sb.movie.entities.User;
import com.sb.movie.enums.SeatStatus;
import com.sb.movie.events.BookingConfirmedEvent;
import com.sb.movie.events.BookingFailedEvent;
import com.sb.movie.exceptions.SeatsNotAvailable;
import com.sb.movie.exceptions.ShowDoesNotExists;
import com.sb.movie.exceptions.UserDoesNotExists;
import com.sb.movie.repositories.ShowRepository;
import com.sb.movie.repositories.ShowSeatRepository;
import com.sb.movie.repositories.TicketRepository;
import com.sb.movie.repositories.UserRepository;
import com.sb.movie.request.SeatLockRequest;
import com.sb.movie.request.TicketRequest;
import com.sb.movie.response.SeatLockResponse;
import com.sb.movie.response.TicketHistoryResponse;
import com.sb.movie.response.TicketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatLockingService seatLockingService;
    private final BookingEventProducer bookingEventProducer;

    @Value("${booking.seat-lock-timeout:10}")
    private int seatLockTimeoutMinutes;

    @Override
    @Transactional
    @CacheEvict(value = "showById", key = "#seatLockRequest.showId")
    public SeatLockResponse lockSeats(SeatLockRequest seatLockRequest) {
        log.info("Locking seats for user {} for show {}",
                seatLockRequest.getUserId(), seatLockRequest.getShowId());

        // Validate show exists
        Optional<Show> showOpt = showRepository.findById(seatLockRequest.getShowId());
        if (showOpt.isEmpty()) {
            throw new ShowDoesNotExists();
        }

        // Validate user exists
        Optional<User> userOpt = userRepository.findById(seatLockRequest.getUserId());
        if (userOpt.isEmpty()) {
            throw new UserDoesNotExists();
        }

        Show show = showOpt.get();
        User user = userOpt.get();

        // Get seat IDs from seat numbers
        List<Integer> seatIds = getSeatIdsBySeatNumbers(
                show.getShowSeatList(),
                seatLockRequest.getRequestSeats()
        );

        if (seatIds.isEmpty()) {
            throw new SeatsNotAvailable("Requested seats not found");
        }

        // Lock the seats
        List<ShowSeat> lockedSeats = seatLockingService.lockSeats(seatIds, user.getId());

        // Calculate total price
        Integer totalPrice = lockedSeats.stream()
                .mapToInt(ShowSeat::getPrice)
                .sum();

        // Calculate expiry time
        LocalDateTime lockedAt = lockedSeats.get(0).getLockedAt();
        LocalDateTime expiresAt = lockedAt.plusMinutes(seatLockTimeoutMinutes);

        log.info("Successfully locked {} seats for user {}. Expires at {}",
                lockedSeats.size(), user.getId(), expiresAt);

        return SeatLockResponse.builder()
                .lockedSeats(seatLockRequest.getRequestSeats())
                .totalPrice(totalPrice)
                .lockedAt(lockedAt)
                .expiresAt(expiresAt)
                .message("Seats locked successfully. Please complete booking within " +
                        seatLockTimeoutMinutes + " minutes.")
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "showById", key = "#seatLockRequest.showId")
    public void releaseSeats(SeatLockRequest seatLockRequest) {
        log.info("Releasing seats for user {} for show {}",
                seatLockRequest.getUserId(), seatLockRequest.getShowId());

        // Validate show exists
        Optional<Show> showOpt = showRepository.findById(seatLockRequest.getShowId());
        if (showOpt.isEmpty()) {
            throw new ShowDoesNotExists();
        }

        Show show = showOpt.get();

        // Get seat IDs from seat numbers
        List<Integer> seatIds = getSeatIdsBySeatNumbers(
                show.getShowSeatList(),
                seatLockRequest.getRequestSeats()
        );

        if (!seatIds.isEmpty()) {
            seatLockingService.releaseSeats(seatIds, seatLockRequest.getUserId());
            log.info("Successfully released {} seats for user {}",
                    seatIds.size(), seatLockRequest.getUserId());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "showById", key = "#ticketRequest.showId")
    public TicketResponse ticketBooking(TicketRequest ticketRequest) {
        log.info("Processing ticket booking for user {} for show {}",
                ticketRequest.getUserId(), ticketRequest.getShowId());

        // Validate show exists
        Optional<Show> showOpt = showRepository.findById(ticketRequest.getShowId());
        if (showOpt.isEmpty()) {
            throw new ShowDoesNotExists();
        }

        // Validate user exists
        Optional<User> userOpt = userRepository.findById(ticketRequest.getUserId());
        if (userOpt.isEmpty()) {
            throw new UserDoesNotExists();
        }

        User user = userOpt.get();
        Show show = showOpt.get();

        // Get seat IDs from seat numbers
        List<Integer> seatIds = getSeatIdsBySeatNumbers(
                show.getShowSeatList(),
                ticketRequest.getRequestSeats()
        );

        if (seatIds.isEmpty()) {
            publishBookingFailedEvent(user, show, ticketRequest.getRequestSeats(),
                    "Requested seats not found");
            throw new SeatsNotAvailable("Requested seats not found");
        }

        try {
            // Step 1: Get the seats with pessimistic lock to check their current status
            List<ShowSeat> seats = showSeatRepository.findAndLockByIds(seatIds);

            // Verify we found all requested seats
            if (seats.size() != seatIds.size()) {
                throw new SeatsNotAvailable("Some seats not found");
            }

            // Check if seats are already locked by this user
            boolean alreadyLockedByUser = seats.stream()
                    .allMatch(seat -> seat.getStatus() == SeatStatus.LOCKED &&
                             seat.getLockedByUserId() != null &&
                             seat.getLockedByUserId().equals(user.getId()));

            List<ShowSeat> seatsToBook;

            if (alreadyLockedByUser) {
                // Seats already locked by this user from /lock-seats endpoint
                log.info("Seats already locked by user {}, proceeding with booking", user.getId());
                seatsToBook = seats;
            } else {
                // Check if seats are available for locking
                boolean allAvailable = seats.stream()
                        .allMatch(seat -> seat.getStatus() == SeatStatus.AVAILABLE);

                if (!allAvailable) {
                    // Seats are locked by someone else or already booked
                    throw new SeatsNotAvailable("One or more seats are not available");
                }

                // Lock the seats (for backward compatibility with direct booking)
                log.info("Seats are available, locking them for user {} before booking", user.getId());
                LocalDateTime lockTime = LocalDateTime.now();
                for (ShowSeat seat : seats) {
                    seat.setStatus(SeatStatus.LOCKED);
                    seat.setLockedAt(lockTime);
                    seat.setLockedByUserId(user.getId());
                }
                showSeatRepository.saveAll(seats);
                seatsToBook = seats;
            }

            // Step 2: Calculate total price
            Integer totalPrice = seatsToBook.stream()
                    .mapToInt(ShowSeat::getPrice)
                    .sum();

            String seatsStr = listToString(ticketRequest.getRequestSeats());

            // Step 3: Create ticket
            Ticket ticket = new Ticket();
            ticket.setTotalTicketsPrice(totalPrice);
            ticket.setBookedSeats(seatsStr);
            ticket.setUser(user);
            ticket.setShow(show);

            ticket = ticketRepository.save(ticket);

            // Step 4: Confirm booking (change status from LOCKED to BOOKED)
            seatLockingService.confirmBooking(seatIds, user.getId());

            log.info("Successfully created booking {} for user {}", ticket.getTicketId(), user.getId());

            // Step 5: Publish booking confirmed event to Kafka
            publishBookingConfirmedEvent(ticket, user, show, seatsToBook);

            return TicketConvertor.returnTicket(show, ticket);

        } catch (IllegalStateException e) {
            log.error("Failed to book seats for user {}: {}", user.getId(), e.getMessage());
            publishBookingFailedEvent(user, show, ticketRequest.getRequestSeats(), e.getMessage());
            throw new SeatsNotAvailable(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during booking for user {}: {}", user.getId(), e.getMessage());
            // Release locks if anything goes wrong
            try {
                seatLockingService.releaseSeats(seatIds, user.getId());
            } catch (Exception releaseError) {
                log.error("Failed to release seats: {}", releaseError.getMessage());
            }
            publishBookingFailedEvent(user, show, ticketRequest.getRequestSeats(),
                    "Booking system error: " + e.getMessage());
            throw new RuntimeException("Booking failed: " + e.getMessage(), e);
        }
    }

    /**
     * Publishes booking confirmed event to Kafka for email notification
     */
    private void publishBookingConfirmedEvent(Ticket ticket, User user, Show show, List<ShowSeat> seats) {
        try {
            String bookingReference = "BMS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            BookingConfirmedEvent event = BookingConfirmedEvent.builder()
                    .bookingId(ticket.getTicketId())
                    .bookingReference(bookingReference)
                    .userId(user.getId())
                    .userEmail(user.getEmailId())
                    .userName(user.getName())
                    .userMobile(user.getMobileNo())
                    .showId(show.getShowId())
                    .eventName(show.getEvent().getName())
                    .eventType(show.getEvent().getEventType().toString())
                    .theaterName(show.getTheater().getName())
                    .theaterAddress(show.getTheater().getAddress())
                    .showTime(LocalDateTime.of(show.getDate().toLocalDate(), show.getTime().toLocalTime()))
                    .bookedSeats(ticket.getBookedSeats())
                    .totalSeats(seats.size())
                    .totalPrice(ticket.getTotalTicketsPrice())
                    .bookingTime(LocalDateTime.now())
                    .build();

            bookingEventProducer.publishBookingConfirmed(event);
        } catch (Exception e) {
            // Don't fail the booking if Kafka publishing fails
            log.error("Failed to publish booking confirmed event for ticket {}: {}",
                    ticket.getTicketId(), e.getMessage());
        }
    }

    /**
     * Publishes booking failed event to Kafka for notification
     */
    private void publishBookingFailedEvent(User user, Show show, List<String> requestedSeats, String reason) {
        try {
            BookingFailedEvent event = BookingFailedEvent.builder()
                    .userId(user.getId())
                    .userEmail(user.getEmailId())
                    .showId(show.getShowId())
                    .eventName(show.getEvent().getName())
                    .requestedSeats(String.join(", ", requestedSeats))
                    .failureReason(reason)
                    .failureTime(LocalDateTime.now())
                    .build();

            bookingEventProducer.publishBookingFailed(event);
        } catch (Exception e) {
            // Don't fail the booking process if Kafka publishing fails
            log.error("Failed to publish booking failed event for user {}: {}",
                    user.getId(), e.getMessage());
        }
    }

    /**
     * Convert seat numbers to seat IDs
     */
    private List<Integer> getSeatIdsBySeatNumbers(List<ShowSeat> showSeatList, List<String> requestSeats) {
        return showSeatList.stream()
                .filter(seat -> requestSeats.contains(seat.getSeatNo()))
                .map(ShowSeat::getId)
                .collect(Collectors.toList());
    }

    private String listToString(List<String> requestSeats) {
        return String.join(", ", requestSeats);
    }

    @Override
    public List<TicketHistoryResponse> getMyTickets(String userEmail) {
        User user = userRepository.findByEmailId(userEmail)
                .orElseThrow(() -> new UserDoesNotExists());

        List<Ticket> tickets = ticketRepository.findByUser(user);
        return tickets.stream()
                .map(this::convertToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TicketHistoryResponse getTicketById(Integer ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return convertToHistoryResponse(ticket);
    }

    @Override
    @Transactional
    public String cancelTicket(Integer ticketId, String userEmail) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        User user = userRepository.findByEmailId(userEmail)
                .orElseThrow(() -> new UserDoesNotExists());

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to cancel this ticket");
        }

        Show show = ticket.getShow();
        LocalDate showDate = show.getDate().toLocalDate();
        LocalDate currentDate = LocalDate.now();

        if (showDate.isBefore(currentDate)) {
            throw new RuntimeException("Cannot cancel ticket for past shows");
        }

        LocalDateTime showDateTime = LocalDateTime.of(showDate, show.getTime().toLocalTime());
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilShow = java.time.Duration.between(now, showDateTime).toHours();

        if (hoursUntilShow < 2) {
            throw new RuntimeException("Cannot cancel ticket less than 2 hours before show");
        }

        String[] seatNumbers = ticket.getBookedSeats().split(", ");
        List<Integer> seatIds = getSeatIdsBySeatNumbers(show.getShowSeatList(), List.of(seatNumbers));

        List<ShowSeat> seats = showSeatRepository.findAllById(seatIds);
        for (ShowSeat seat : seats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedByUserId(null);
            seat.setLockedAt(null);
        }
        showSeatRepository.saveAll(seats);

        ticketRepository.delete(ticket);

        String refundMessage;
        if (hoursUntilShow >= 24) {
            refundMessage = "Ticket cancelled successfully. 100% refund will be processed.";
        } else {
            refundMessage = "Ticket cancelled successfully. 50% refund will be processed.";
        }

        log.info("Ticket {} cancelled by user {}", ticketId, user.getId());
        return refundMessage;
    }

    private TicketHistoryResponse convertToHistoryResponse(Ticket ticket) {
        Show show = ticket.getShow();
        return TicketHistoryResponse.builder()
                .ticketId(ticket.getTicketId())
                .bookedAt(ticket.getBookedAt())
                .totalPrice(ticket.getTotalTicketsPrice())
                .bookedSeats(ticket.getBookedSeats())
                .showId(show.getShowId())
                .showDate(show.getDate())
                .showTime(show.getTime())
                .eventName(show.getEvent().getName())
                .eventType(show.getEvent().getEventType().toString())
                .theaterName(show.getTheater().getName())
                .theaterAddress(show.getTheater().getAddress())
                .city(show.getTheater().getCity())
                .build();
    }
}
