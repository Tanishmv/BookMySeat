package com.sb.movie.services;

import com.sb.movie.events.BookingConfirmedEvent;
import com.sb.movie.events.BookingFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailNotificationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    /**
     * Simulates sending a booking confirmation email
     * In production, this would integrate with an email service (SendGrid, SES, etc.)
     */
    public void sendBookingConfirmationEmail(BookingConfirmedEvent event) {
        log.info("=".repeat(100));
        log.info("SENDING BOOKING CONFIRMATION EMAIL");
        log.info("=".repeat(100));

        String emailBody = buildBookingConfirmationEmail(event);

        log.info("\n{}", emailBody);

        log.info("=".repeat(100));
        log.info("EMAIL SENT SUCCESSFULLY TO: {}", event.getUserEmail());
        log.info("=".repeat(100));
    }

    /**
     * Simulates sending a booking failure notification email
     */
    public void sendBookingFailureEmail(BookingFailedEvent event) {
        log.warn("=".repeat(100));
        log.warn("SENDING BOOKING FAILURE NOTIFICATION EMAIL");
        log.warn("=".repeat(100));

        String emailBody = buildBookingFailureEmail(event);

        log.warn("\n{}", emailBody);

        log.warn("=".repeat(100));
        log.warn("EMAIL SENT SUCCESSFULLY TO: {}", event.getUserEmail());
        log.warn("=".repeat(100));
    }

    private String buildBookingConfirmationEmail(BookingConfirmedEvent event) {
        StringBuilder email = new StringBuilder();

        email.append("\n");
        email.append("=".repeat(80)).append("\n");
        email.append("                    BOOKING CONFIRMATION - BookMySeat                    \n");
        email.append("=".repeat(80)).append("\n");
        email.append("\n");

        // Header
        email.append("TO: ").append(event.getUserEmail()).append("\n");
        email.append("SUBJECT: Booking Confirmed - ").append(event.getEventName()).append("\n");
        email.append("\n");
        email.append("-".repeat(80)).append("\n");
        email.append("\n");

        // Greeting
        email.append("Dear ").append(event.getUserName()).append(",\n");
        email.append("\n");
        email.append("Great news! Your booking has been confirmed successfully.\n");
        email.append("\n");

        // Booking Details Box
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("|").append(centerText("BOOKING DETAILS", 78)).append("|\n");
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append(formatTableRow("Booking Reference", event.getBookingReference()));
        email.append(formatTableRow("Booking ID", "#" + event.getBookingId()));
        email.append(formatTableRow("Booking Date", event.getBookingTime().format(DATE_TIME_FORMATTER)));
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("\n");

        // Event Details Box
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("|").append(centerText("EVENT DETAILS", 78)).append("|\n");
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append(formatTableRow("Event Name", event.getEventName()));
        email.append(formatTableRow("Event Type", event.getEventType()));
        email.append(formatTableRow("Show Date & Time", event.getShowTime().format(DATE_TIME_FORMATTER)));
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("\n");

        // Venue Details Box
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("|").append(centerText("VENUE DETAILS", 78)).append("|\n");
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append(formatTableRow("Theater Name", event.getTheaterName()));
        email.append(formatTableRow("Address", event.getTheaterAddress()));
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("\n");

        // Seat Details Box
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("|").append(centerText("SEAT DETAILS", 78)).append("|\n");
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append(formatTableRow("Number of Seats", String.valueOf(event.getTotalSeats())));
        email.append(formatTableRow("Seat Numbers", event.getBookedSeats()));
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("\n");

        // Payment Details Box
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("|").append(centerText("PAYMENT DETAILS", 78)).append("|\n");
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append(formatTableRow("Total Amount Paid", "Rs." + event.getTotalPrice()));
        email.append(formatTableRow("Payment Status", "CONFIRMED"));
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("\n");

        // Contact Information
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("|").append(centerText("CONTACT INFORMATION", 78)).append("|\n");
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append(formatTableRow("Name", event.getUserName()));
        email.append(formatTableRow("Email", event.getUserEmail()));
        email.append(formatTableRow("Mobile", event.getUserMobile() != null ? event.getUserMobile() : "N/A"));
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("\n");

        // Important Notes
        email.append("IMPORTANT NOTES:\n");
        email.append("  * Please arrive at the venue at least 15 minutes before the show time\n");
        email.append("  * Carry a valid photo ID for verification\n");
        email.append("  * Show this confirmation email at the entrance\n");
        email.append("  * Outside food and beverages are not allowed\n");
        email.append("  * Smoking and use of mobile phones during the show is strictly prohibited\n");
        email.append("\n");

        // Footer
        email.append("-".repeat(80)).append("\n");
        email.append("\n");
        email.append("Thank you for choosing BookMySeat!\n");
        email.append("For any queries, contact us at support@bookmyseat.com or call 1800-XXX-XXXX\n");
        email.append("\n");
        email.append("=".repeat(80)).append("\n");
        email.append("This is an automated email. Please do not reply to this message.\n");
        email.append("(c) 2025 BookMySeat. All rights reserved.\n");
        email.append("=".repeat(80)).append("\n");

        return email.toString();
    }

    private String buildBookingFailureEmail(BookingFailedEvent event) {
        StringBuilder email = new StringBuilder();

        email.append("\n");
        email.append("=".repeat(80)).append("\n");
        email.append("                    BOOKING FAILED - BookMySeat                    \n");
        email.append("=".repeat(80)).append("\n");
        email.append("\n");

        // Header
        email.append("TO: ").append(event.getUserEmail()).append("\n");
        email.append("SUBJECT: Booking Failed - ").append(event.getEventName()).append("\n");
        email.append("\n");
        email.append("-".repeat(80)).append("\n");
        email.append("\n");

        // Message
        email.append("Dear Customer,\n");
        email.append("\n");
        email.append("We regret to inform you that your booking attempt was unsuccessful.\n");
        email.append("\n");

        // Failure Details Box
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("|").append(centerText("BOOKING ATTEMPT DETAILS", 78)).append("|\n");
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append(formatTableRow("Event Name", event.getEventName()));
        email.append(formatTableRow("Requested Seats", event.getRequestedSeats()));
        email.append(formatTableRow("Failure Time", event.getFailureTime().format(DATE_TIME_FORMATTER)));
        email.append(formatTableRow("Reason", event.getFailureReason()));
        email.append("+").append("-".repeat(78)).append("+\n");
        email.append("\n");

        // Suggestions
        email.append("WHAT YOU CAN DO:\n");
        email.append("  * Try booking different seats for the same show\n");
        email.append("  * Check availability for other show timings\n");
        email.append("  * Contact our support team for assistance\n");
        email.append("\n");

        // Footer
        email.append("-".repeat(80)).append("\n");
        email.append("\n");
        email.append("We apologize for the inconvenience.\n");
        email.append("For assistance, contact us at support@bookmyseat.com or call 1800-XXX-XXXX\n");
        email.append("\n");
        email.append("=".repeat(80)).append("\n");
        email.append("This is an automated email. Please do not reply to this message.\n");
        email.append("(c) 2025 BookMySeat. All rights reserved.\n");
        email.append("=".repeat(80)).append("\n");

        return email.toString();
    }

    private String formatTableRow(String label, String value) {
        String formattedLabel = String.format("| %-25s : ", label);
        String formattedValue = String.format("%-49s |\n", value);
        return formattedLabel + formattedValue;
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }
}
