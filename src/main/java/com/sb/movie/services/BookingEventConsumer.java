package com.sb.movie.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb.movie.config.KafkaConfig;
import com.sb.movie.events.BookingConfirmedEvent;
import com.sb.movie.events.BookingFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {

    private final EmailNotificationService emailNotificationService;
    private final ObjectMapper objectMapper;

    /**
     * Consumes booking confirmed events and sends email notifications
     * Uses manual acknowledgment to ensure reliable message processing
     */
    @KafkaListener(
            topics = KafkaConfig.BOOKING_CONFIRMED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:bookmyseat-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeBookingConfirmed(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("=".repeat(100));
        log.info("KAFKA CONSUMER: Received BookingConfirmedEvent from partition: {} at offset: {}",
                partition, offset);
        log.info("=".repeat(100));

        try {
            // Deserialize the event
            BookingConfirmedEvent event = objectMapper.readValue(message, BookingConfirmedEvent.class);

            log.info("Processing booking confirmation for:");
            log.info("  * Booking ID: {}", event.getBookingId());
            log.info("  * Reference: {}", event.getBookingReference());
            log.info("  * User: {} ({})", event.getUserName(), event.getUserEmail());
            log.info("  * Event: {} ({})", event.getEventName(), event.getEventType());
            log.info("  * Theater: {}", event.getTheaterName());
            log.info("  * Seats: {} (Total: {})", event.getBookedSeats(), event.getTotalSeats());
            log.info("  * Total Price: Rs.{}", event.getTotalPrice());
            log.info("  * Show Time: {}", event.getShowTime());

            // Simulate email sending
            emailNotificationService.sendBookingConfirmationEmail(event);

            // Additional processing could be added here:
            // - Send SMS notification
            // - Update analytics/metrics
            // - Store in data warehouse
            // - Trigger third-party integrations

            log.info("[SUCCESS] Processed BookingConfirmedEvent for booking ID: {}",
                    event.getBookingId());

            // Manually acknowledge the message
            acknowledgment.acknowledge();
            log.debug("Message acknowledged for booking ID: {}", event.getBookingId());

        } catch (Exception e) {
            log.error("[ERROR] Failed to process BookingConfirmedEvent from partition: {} at offset: {}. Error: {}",
                    partition, offset, e.getMessage(), e);

            // In a production system, you might want to:
            // 1. Send to a dead-letter queue (DLQ)
            // 2. Implement retry logic
            // 3. Alert monitoring systems
            // For now, we'll acknowledge to prevent reprocessing
            acknowledgment.acknowledge();
            log.warn("Message acknowledged despite processing failure to prevent infinite retries");
        }

        log.info("=".repeat(100));
    }

    /**
     * Consumes booking failed events and sends notification emails
     */
    @KafkaListener(
            topics = KafkaConfig.BOOKING_FAILED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:bookmyseat-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeBookingFailed(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.warn("=".repeat(100));
        log.warn("KAFKA CONSUMER: Received BookingFailedEvent from partition: {} at offset: {}",
                partition, offset);
        log.warn("=".repeat(100));

        try {
            // Deserialize the event
            BookingFailedEvent event = objectMapper.readValue(message, BookingFailedEvent.class);

            log.warn("Processing booking failure notification for:");
            log.warn("  • User Email: {}", event.getUserEmail());
            log.warn("  • Event: {}", event.getEventName());
            log.warn("  • Requested Seats: {}", event.getRequestedSeats());
            log.warn("  • Failure Reason: {}", event.getFailureReason());
            log.warn("  • Failure Time: {}", event.getFailureTime());

            // Send failure notification email
            emailNotificationService.sendBookingFailureEmail(event);

            // Additional processing:
            // - Track failure metrics
            // - Analyze failure patterns
            // - Update seat availability cache if needed

            log.warn("[SUCCESS] Processed BookingFailedEvent for user: {}", event.getUserEmail());

            // Acknowledge the message
            acknowledgment.acknowledge();
            log.debug("Message acknowledged for failed booking notification");

        } catch (Exception e) {
            log.error("[ERROR] Failed to process BookingFailedEvent from partition: {} at offset: {}. Error: {}",
                    partition, offset, e.getMessage(), e);

            // Acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
            log.warn("Message acknowledged despite processing failure");
        }

        log.warn("=".repeat(100));
    }
}
