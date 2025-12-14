package com.sb.movie.repositories;

import com.sb.movie.entities.ShowSeat;
import com.sb.movie.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.id IN :seatIds")
    List<ShowSeat> findAndLockByIds(@Param("seatIds") List<Integer> seatIds);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.showId = :showId AND ss.status = 'AVAILABLE'")
    List<ShowSeat> findAvailableSeatsByShowId(@Param("showId") Integer showId);

    @Query("SELECT COUNT(ss) FROM ShowSeat ss WHERE ss.show.showId = :showId AND ss.status = 'AVAILABLE'")
    Long countAvailableSeatsByShowId(@Param("showId") Integer showId);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.status = 'LOCKED' AND ss.lockedAt < :expiryTime")
    List<ShowSeat> findExpiredLockedSeats(@Param("expiryTime") LocalDateTime expiryTime);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'AVAILABLE', ss.lockedAt = NULL, ss.lockedByUserId = NULL " +
           "WHERE ss.status = 'LOCKED' AND ss.lockedAt < :expiryTime")
    int releaseExpiredLocks(@Param("expiryTime") LocalDateTime expiryTime);
}
