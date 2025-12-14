package com.sb.movie.services;

import com.sb.movie.config.KafkaConfig;
import com.sb.movie.events.BookingConfirmedEvent;
import com.sb.movie.events.BookingFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a booking confirmed event to Kafka
     */
    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Publishing BookingConfirmedEvent for booking ID: {} to topic: {}",
                event.getBookingId(), KafkaConfig.BOOKING_CONFIRMED_TOPIC);

        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    KafkaConfig.BOOKING_CONFIRMED_TOPIC,
                    event.getBookingReference(),
                    event
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published BookingConfirmedEvent for booking ID: {} to partition: {} with offset: {}",
                            event.getBookingId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    log.debug("Event details - User: {}, Event: {}, Seats: {}, Price: {}",
                            event.getUserEmail(), event.getEventName(),
                            event.getBookedSeats(), event.getTotalPrice());
                } else {
                    log.error("Failed to publish BookingConfirmedEvent for booking ID: {}. Error: {}",
                            event.getBookingId(), ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Exception occurred while publishing BookingConfirmedEvent for booking ID: {}. Error: {}",
                    event.getBookingId(), e.getMessage(), e);
        }
    }

    /**
     * Publishes a booking failed event to Kafka
     */
    public void publishBookingFailed(BookingFailedEvent event) {
        log.info("Publishing BookingFailedEvent for user: {} to topic: {}",
                event.getUserEmail(), KafkaConfig.BOOKING_FAILED_TOPIC);

        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    KafkaConfig.BOOKING_FAILED_TOPIC,
                    event.getUserEmail(),
                    event
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published BookingFailedEvent for user: {} to partition: {} with offset: {}",
                            event.getUserEmail(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    log.debug("Failure reason: {}", event.getFailureReason());
                } else {
                    log.error("Failed to publish BookingFailedEvent for user: {}. Error: {}",
                            event.getUserEmail(), ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Exception occurred while publishing BookingFailedEvent for user: {}. Error: {}",
                    event.getUserEmail(), e.getMessage(), e);
        }
    }
}
