package com.sb.movie.services;

import com.sb.movie.entities.ShowSeat;
import com.sb.movie.enums.SeatStatus;
import com.sb.movie.repositories.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatLockingService {

    private final ShowSeatRepository showSeatRepository;

    @Value("${booking.seat-lock-timeout:10}")
    private int seatLockTimeoutMinutes;

    /**
     * Lock seats for a user with pessimistic locking to prevent double-booking.
     * Uses SERIALIZABLE isolation level for maximum safety.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<ShowSeat> lockSeats(List<Integer> seatIds, Integer userId) {
        log.info("Attempting to lock {} seats for user {}", seatIds.size(), userId);

        // Acquire pessimistic write lock on the seats
        List<ShowSeat> seats = showSeatRepository.findAndLockByIds(seatIds);

        // Verify we found all requested seats
        if (seats.size() != seatIds.size()) {
            throw new IllegalArgumentException("Some seats not found");
        }

        // Check if all seats are available
        for (ShowSeat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalStateException("Seat " + seat.getSeatNo() +
                        " is not available (Status: " + seat.getStatus() + ")");
            }
        }

        // Lock all seats
        LocalDateTime lockTime = LocalDateTime.now();
        for (ShowSeat seat : seats) {
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockedAt(lockTime);
            seat.setLockedByUserId(userId);
        }

        showSeatRepository.saveAll(seats);
        log.info("Successfully locked {} seats for user {}", seats.size(), userId);

        return seats;
    }

    /**
     * Confirm booking by changing seat status from LOCKED to BOOKED.
     * Only the user who locked the seats can confirm them.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void confirmBooking(List<Integer> seatIds, Integer userId) {
        log.info("Confirming booking for {} seats by user {}", seatIds.size(), userId);

        List<ShowSeat> seats = showSeatRepository.findAndLockByIds(seatIds);

        for (ShowSeat seat : seats) {
            if (seat.getStatus() != SeatStatus.LOCKED) {
                throw new IllegalStateException("Seat " + seat.getSeatNo() + " is not locked");
            }

            if (!seat.getLockedByUserId().equals(userId)) {
                throw new IllegalStateException("Seat " + seat.getSeatNo() +
                        " is locked by another user");
            }

            seat.setStatus(SeatStatus.BOOKED);
            seat.setLockedAt(null);
            seat.setLockedByUserId(null);
        }

        showSeatRepository.saveAll(seats);
        log.info("Successfully confirmed booking for {} seats", seats.size());
    }

    /**
     * Release locks on seats (e.g., when user cancels or payment fails).
     */
    @Transactional
    public void releaseSeats(List<Integer> seatIds, Integer userId) {
        log.info("Releasing {} seats for user {}", seatIds.size(), userId);

        List<ShowSeat> seats = showSeatRepository.findAllById(seatIds);

        for (ShowSeat seat : seats) {
            if (seat.getStatus() == SeatStatus.LOCKED &&
                    seat.getLockedByUserId().equals(userId)) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setLockedAt(null);
                seat.setLockedByUserId(null);
            }
        }

        showSeatRepository.saveAll(seats);
        log.info("Successfully released {} seats", seats.size());
    }

    /**
     * Scheduled task to release expired seat locks.
     * Runs every 2 minutes.
     */
    @Scheduled(fixedDelay = 120000) // 2 minutes
    @Transactional
    public void releaseExpiredLocks() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(seatLockTimeoutMinutes);

        int releasedCount = showSeatRepository.releaseExpiredLocks(expiryTime);

        if (releasedCount > 0) {
            log.info("Released {} expired seat locks", releasedCount);
        }
    }

    /**
     * Check if seats are available for locking.
     */
    @Transactional(readOnly = true)
    public boolean areSeatsAvailable(List<Integer> seatIds) {
        List<ShowSeat> seats = showSeatRepository.findAllById(seatIds);

        if (seats.size() != seatIds.size()) {
            return false;
        }

        return seats.stream().allMatch(seat -> seat.getStatus() == SeatStatus.AVAILABLE);
    }
}
