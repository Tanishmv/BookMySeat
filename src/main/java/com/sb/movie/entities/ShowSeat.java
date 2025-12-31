package com.sb.movie.entities;

import com.sb.movie.enums.SeatStatus;
import com.sb.movie.enums.SeatType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHOW_SEATS", indexes = {
    @Index(name = "idx_show_seat", columnList = "show_showId,seatNo"),
    @Index(name = "idx_seat_status", columnList = "status")
})
@Data
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String seatNo;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType;

    private Integer price;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    private LocalDateTime lockedAt;

    private Integer lockedByUserId;

    @Version
    private Long version;  // Optimistic locking

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_showId", nullable = false)
    private Show show;

    public Boolean getIsAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public void setIsAvailable(Boolean available) {
        this.status = available ? SeatStatus.AVAILABLE : SeatStatus.BOOKED;
    }
}

