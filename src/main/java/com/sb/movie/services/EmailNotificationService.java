package com.sb.movie.services;

import com.sb.movie.events.BookingConfirmedEvent;
import com.sb.movie.events.BookingFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@bookmyseat.com}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    /**
     * Sends a booking confirmation email
     */
    public void sendBookingConfirmationEmail(BookingConfirmedEvent event) {
        log.info("=".repeat(100));
        log.info("SENDING BOOKING CONFIRMATION EMAIL");
        log.info("=".repeat(100));

        try {
            // Build and log the text version of the email for simulation
            String textEmail = buildBookingConfirmationEmail(event);
            log.info("EMAIL SIMULATION:\n{}", textEmail);

            String htmlBody = buildBookingConfirmationEmailHtml(event);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(fromEmail);
            helper.setTo(event.getUserEmail());
            helper.setSubject("Booking Confirmed - " + event.getEventName() + " - BookMySeat");
            helper.setText(htmlBody, true); // true = HTML content

            mailSender.send(mimeMessage);

            log.info("=".repeat(100));
            log.info("HTML EMAIL SENT SUCCESSFULLY TO: {}", event.getUserEmail());
            log.info("=".repeat(100));

        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to {}: {}",
                    event.getUserEmail(), e.getMessage());
            // Don't throw exception - email failure shouldn't break the booking
        }
    }

    /**
     * Sends a booking failure notification email
     */
    public void sendBookingFailureEmail(BookingFailedEvent event) {
        log.warn("=".repeat(100));
        log.warn("SENDING BOOKING FAILURE NOTIFICATION EMAIL");
        log.warn("=".repeat(100));

        try {
            // Build and log the text version of the email for simulation
            String textEmail = buildBookingFailureEmail(event);
            log.warn("EMAIL SIMULATION:\n{}", textEmail);

            String htmlBody = buildBookingFailureEmailHtml(event);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(fromEmail);
            helper.setTo(event.getUserEmail());
            helper.setSubject("Booking Failed - " + event.getEventName() + " - BookMySeat");
            helper.setText(htmlBody, true); // true = HTML content

            mailSender.send(mimeMessage);

            log.warn("=".repeat(100));
            log.warn("HTML EMAIL SENT SUCCESSFULLY TO: {}", event.getUserEmail());
            log.warn("=".repeat(100));

        } catch (Exception e) {
            log.error("Failed to send booking failure email to {}: {}",
                    event.getUserEmail(), e.getMessage());
            // Don't throw exception - email failure shouldn't break the flow
        }
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

    /**
     * Builds HTML booking confirmation email
     */
    private String buildBookingConfirmationEmailHtml(BookingConfirmedEvent event) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f4f4f4; padding: 20px 0;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" border="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">

                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: bold;">Booking Confirmed</h1>
                                        <p style="margin: 10px 0 0 0; color: #ffffff; font-size: 14px;">BookMySeat</p>
                                    </td>
                                </tr>

                                <!-- Content -->
                                <tr>
                                    <td style="padding: 30px;">
                                        <p style="margin: 0 0 20px 0; font-size: 16px; color: #333333;">Dear <strong>%s</strong>,</p>
                                        <p style="margin: 0 0 30px 0; font-size: 14px; color: #666666; line-height: 1.6;">
                                            Great news! Your booking has been confirmed successfully. Below are your booking details:
                                        </p>

                                        <!-- Booking Details -->
                                        <table width="100%%" cellpadding="15" cellspacing="0" border="0" style="background-color: #f8f9fa; border-radius: 6px; margin-bottom: 20px;">
                                            <tr>
                                                <td colspan="2" style="border-bottom: 2px solid #667eea; padding-bottom: 10px;">
                                                    <h2 style="margin: 0; font-size: 18px; color: #667eea;">Booking Details</h2>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666; width: 40%%;">Booking Reference</td>
                                                <td style="font-size: 14px; color: #333333; font-weight: bold;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666;">Booking ID</td>
                                                <td style="font-size: 14px; color: #333333; font-weight: bold;">#%s</td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666;">Booking Date</td>
                                                <td style="font-size: 14px; color: #333333;">%s</td>
                                            </tr>
                                        </table>

                                        <!-- Event Details -->
                                        <table width="100%%" cellpadding="15" cellspacing="0" border="0" style="background-color: #f8f9fa; border-radius: 6px; margin-bottom: 20px;">
                                            <tr>
                                                <td colspan="2" style="border-bottom: 2px solid #667eea; padding-bottom: 10px;">
                                                    <h2 style="margin: 0; font-size: 18px; color: #667eea;">Event Details</h2>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666; width: 40%%;">Event Name</td>
                                                <td style="font-size: 14px; color: #333333; font-weight: bold;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666;">Event Type</td>
                                                <td style="font-size: 14px; color: #333333;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666;">Show Date & Time</td>
                                                <td style="font-size: 14px; color: #333333;">%s</td>
                                            </tr>
                                        </table>

                                        <!-- Venue Details -->
                                        <table width="100%%" cellpadding="15" cellspacing="0" border="0" style="background-color: #f8f9fa; border-radius: 6px; margin-bottom: 20px;">
                                            <tr>
                                                <td colspan="2" style="border-bottom: 2px solid #667eea; padding-bottom: 10px;">
                                                    <h2 style="margin: 0; font-size: 18px; color: #667eea;">Venue Details</h2>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666; width: 40%%;">Theater Name</td>
                                                <td style="font-size: 14px; color: #333333; font-weight: bold;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666;">Address</td>
                                                <td style="font-size: 14px; color: #333333;">%s</td>
                                            </tr>
                                        </table>

                                        <!-- Seat & Payment Details -->
                                        <table width="100%%" cellpadding="15" cellspacing="0" border="0" style="background-color: #f8f9fa; border-radius: 6px; margin-bottom: 20px;">
                                            <tr>
                                                <td colspan="2" style="border-bottom: 2px solid #667eea; padding-bottom: 10px;">
                                                    <h2 style="margin: 0; font-size: 18px; color: #667eea;">Seat & Payment Details</h2>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666; width: 40%%;">Number of Seats</td>
                                                <td style="font-size: 14px; color: #333333;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666;">Seat Numbers</td>
                                                <td style="font-size: 14px; color: #333333; font-weight: bold;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666;">Total Amount Paid</td>
                                                <td style="font-size: 18px; color: #28a745; font-weight: bold;">Rs. %s</td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666;">Payment Status</td>
                                                <td style="font-size: 14px; color: #28a745; font-weight: bold;">CONFIRMED</td>
                                            </tr>
                                        </table>

                                        <!-- Important Notes -->
                                        <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin-bottom: 20px; border-radius: 4px;">
                                            <h3 style="margin: 0 0 10px 0; font-size: 16px; color: #856404;">Important Notes</h3>
                                            <ul style="margin: 0; padding-left: 20px; font-size: 13px; color: #856404; line-height: 1.8;">
                                                <li>Please arrive at the venue at least 15 minutes before the show time</li>
                                                <li>Carry a valid photo ID for verification</li>
                                                <li>Show this confirmation email at the entrance</li>
                                                <li>Outside food and beverages are not allowed</li>
                                            </ul>
                                        </div>

                                        <p style="margin: 20px 0 0 0; font-size: 14px; color: #666666;">
                                            Thank you for choosing BookMySeat!
                                        </p>
                                    </td>
                                </tr>

                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #dee2e6;">
                                        <p style="margin: 0 0 10px 0; font-size: 13px; color: #666666;">
                                            For any queries, contact us at <a href="mailto:support@bookmyseat.com" style="color: #667eea; text-decoration: none;">support@bookmyseat.com</a>
                                        </p>
                                        <p style="margin: 0; font-size: 12px; color: #999999;">
                                            This is an automated email. Please do not reply to this message.
                                        </p>
                                        <p style="margin: 10px 0 0 0; font-size: 12px; color: #999999;">
                                            &copy; 2025 BookMySeat. All rights reserved.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                event.getUserName(),
                event.getBookingReference(),
                event.getBookingId(),
                event.getBookingTime().format(DATE_TIME_FORMATTER),
                event.getEventName(),
                event.getEventType(),
                event.getShowTime().format(DATE_TIME_FORMATTER),
                event.getTheaterName(),
                event.getTheaterAddress(),
                event.getTotalSeats(),
                event.getBookedSeats(),
                event.getTotalPrice()
            );
    }

    /**
     * Builds HTML booking failure email
     */
    private String buildBookingFailureEmailHtml(BookingFailedEvent event) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f4f4f4; padding: 20px 0;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" border="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">

                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); padding: 30px; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: bold;">Booking Failed</h1>
                                        <p style="margin: 10px 0 0 0; color: #ffffff; font-size: 14px;">BookMySeat</p>
                                    </td>
                                </tr>

                                <!-- Content -->
                                <tr>
                                    <td style="padding: 30px;">
                                        <p style="margin: 0 0 20px 0; font-size: 16px; color: #333333;">Dear Customer,</p>
                                        <p style="margin: 0 0 30px 0; font-size: 14px; color: #666666; line-height: 1.6;">
                                            We regret to inform you that your booking attempt was unsuccessful.
                                        </p>

                                        <!-- Failure Details -->
                                        <table width="100%%" cellpadding="15" cellspacing="0" border="0" style="background-color: #fff5f5; border-radius: 6px; margin-bottom: 20px; border-left: 4px solid #dc3545;">
                                            <tr>
                                                <td colspan="2" style="border-bottom: 2px solid #dc3545; padding-bottom: 10px;">
                                                    <h2 style="margin: 0; font-size: 18px; color: #dc3545;">Booking Attempt Details</h2>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666; width: 40%%;">Event Name</td>
                                                <td style="font-size: 14px; color: #333333; font-weight: bold;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666;">Requested Seats</td>
                                                <td style="font-size: 14px; color: #333333;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666;">Failure Time</td>
                                                <td style="font-size: 14px; color: #333333;">%s</td>
                                            </tr>
                                            <tr>
                                                <td style="font-size: 14px; color: #666666;">Reason</td>
                                                <td style="font-size: 14px; color: #dc3545; font-weight: bold;">%s</td>
                                            </tr>
                                        </table>

                                        <!-- Suggestions -->
                                        <div style="background-color: #d1ecf1; border-left: 4px solid #17a2b8; padding: 15px; margin-bottom: 20px; border-radius: 4px;">
                                            <h3 style="margin: 0 0 10px 0; font-size: 16px; color: #0c5460;">What You Can Do</h3>
                                            <ul style="margin: 0; padding-left: 20px; font-size: 13px; color: #0c5460; line-height: 1.8;">
                                                <li>Try booking different seats for the same show</li>
                                                <li>Check availability for other show timings</li>
                                                <li>Contact our support team for assistance</li>
                                            </ul>
                                        </div>

                                        <p style="margin: 20px 0 0 0; font-size: 14px; color: #666666;">
                                            We apologize for the inconvenience.
                                        </p>
                                    </td>
                                </tr>

                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #dee2e6;">
                                        <p style="margin: 0 0 10px 0; font-size: 13px; color: #666666;">
                                            For assistance, contact us at <a href="mailto:support@bookmyseat.com" style="color: #667eea; text-decoration: none;">support@bookmyseat.com</a>
                                        </p>
                                        <p style="margin: 0; font-size: 12px; color: #999999;">
                                            This is an automated email. Please do not reply to this message.
                                        </p>
                                        <p style="margin: 10px 0 0 0; font-size: 12px; color: #999999;">
                                            &copy; 2025 BookMySeat. All rights reserved.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                event.getEventName(),
                event.getRequestedSeats(),
                event.getFailureTime().format(DATE_TIME_FORMATTER),
                event.getFailureReason()
            );
    }
}
