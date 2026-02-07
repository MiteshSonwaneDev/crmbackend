package com.example.crm_system.service;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import com.example.crm_system.dto.BillDetailsResponseDTO;
import com.example.crm_system.entity.WhatsAppConfig;
import com.example.crm_system.repository.WhatsAppConfigRepository;

@Service
@Slf4j
public class WhatsAppService {

    private static final String API_URL = "https://whatsapp.uikeyservices.in/send-message";
    private static final String API_KEY = "cbmurKsSuaqEL5MOAvNvUPIMtwxw2qhI";
    private static final String SENDER = "917489660550";

    private final RestTemplate restTemplate;
    
    @Autowired
    private WhatsAppConfigRepository whatsAppConfigRepository;

    // ✅ Configure RestTemplate with timeouts
    public WhatsAppService() {
        this.restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(30))  // 30 seconds to connect
            .setReadTimeout(Duration.ofSeconds(30))     // 30 seconds to read response
            .build();
    }

    /**
     * Get the configured doctor WhatsApp number from database
     * Falls back to default if not configured
     */
    private String getDoctorNumber() {
        return whatsAppConfigRepository
                .findFirstByOrderByIdAsc()
                .map(config -> {
                    String number = config.getDoctorWhatsAppNumber();
                    // Ensure number starts with 91
                    return number.startsWith("91") ? number : "91" + number;
                })
                .orElse("919575288606"); // Fallback to default
    }

    /**
     * Check if a specific notification type is enabled
     */
    private boolean isNotificationEnabled(String notificationType) {
        return whatsAppConfigRepository
                .findFirstByOrderByIdAsc()
                .map(config -> {
                    switch (notificationType) {
                        case "APPOINTMENT":
                            return config.getEnableAppointmentNotifications();
                        case "BILL":
                            return config.getEnableBillNotifications();
                        case "LEAD":
                            return config.getEnableLeadNotifications();
                        default:
                            return true;
                    }
                })
                .orElse(true); // Default to enabled if no config found
    }

    public void sendMessage(String phone, String message) {

        try {
            String formattedPhone = phone.startsWith("91") ? phone : "91" + phone;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            // ✅ Add User-Agent header (some APIs require this)
            headers.set("User-Agent", "CRMSystem/1.0");

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("api_key", API_KEY);
            body.add("sender", SENDER);
            body.add("number", formattedPhone);
            body.add("message", message);
            body.add("footer", "Sent via coreconnect");

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            log.info("📤 Sending WhatsApp message");
            log.info("➡️ To: {}", formattedPhone);
            log.info("🔗 API URL: {}", API_URL);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ WhatsApp sent successfully to {}", formattedPhone);
                log.debug("📥 Response: {}", response.getBody());
            } else {
                log.warn("⚠️ WhatsApp API returned non-success status: {}", response.getStatusCode());
                log.warn("📥 Response body: {}", response.getBody());
            }

        } catch (ResourceAccessException e) {
            log.error("❌ Network error - Cannot reach WhatsApp API for {}", phone);
            log.error("🔍 Error details: {}", e.getMessage());
            log.error("💡 This might be a network/firewall issue on the hosting platform");
            // Don't throw exception - just log and continue
        } catch (Exception e) {
            log.error("❌ WhatsApp sending failed for {}: {}", phone, e.getMessage(), e);
        }
    }

    public void sendCustomerWelcome(String customerPhone, String businessName, 
                                   String customerName, String receptionNumber) {

        String message =
                "Dear " + customerName + ",\n" +
                "Greetings from " + businessName + "\n" +
                "Reception Number - " + receptionNumber + "\n" +
                "Start your journey of Wellness for Skin, Hair and body now.\n" +
                "Hope to see you soon\n" +
                "Thank you";

        sendMessage(customerPhone, message);
    }

    // 🔹 Doctor notification message - Now uses dynamic number
    public void sendDoctorNotification(
            String businessName,
            String leadName,
            String leadPhone,
            String source,
            String query) {

        // Check if lead notifications are enabled
        if (!isNotificationEnabled("LEAD")) {
            log.info("⏭️ Lead notifications disabled, skipping");
            return;
        }

        String doctorNumber = getDoctorNumber();
        
        String message =
                businessName + "\n\n" +
                "Greetings from Coreconnect\n\n" +
                "Received new Enquiry\n\n" +
                "Name - " + leadName + "\n" +
                "Number - " + leadPhone + "\n" +
                "For - " + source + "\n" +
                "From - " + query + "\n" +
                "Thank You\n" +
                "Team CoreConnect\n" +
                "7489660550\n";
                
        log.info("📱 Sending lead notification to doctor: {}", doctorNumber);
        sendMessage(doctorNumber, message);
    }

    // 🔹 Customer appointment confirmation
    public void sendAppointmentConfirmation(
            String customerPhone,
            String customerName,
            String businessName,
            String receptionNumber,
            String appointmentDateTime,
            String staffName) {

        String formattedDateTime = formatPrettyDateTime(appointmentDateTime);

        StringBuilder message = new StringBuilder();

        message.append("📅 Appointment Confirmed\n\n")
               .append("Dear ").append(customerName)
               .append(", greetings from ").append(businessName).append("\n\n")
               .append("Your appointment has been scheduled successfully.\n\n")
               .append(formattedDateTime).append(".\n\n")
               .append("Contact Number ").append(receptionNumber).append(".\n\n");

        // ✅ Add Doctor/Staff only if not null or empty
        if (staffName != null && !staffName.trim().isEmpty()) {
            message.append("👩‍⚕️ Doctor/Staff: ").append(staffName).append("\n\n");
        }

        message.append("Thank you for choosing ").append(businessName).append(".");

        sendMessage(customerPhone, message.toString());
    }

    // 🔹 Doctor appointment notification - Now uses dynamic number
    public void sendDoctorAppointmentAlert(
            String businessName,
            String customerName,
            String customerPhone,
            String appointmentDateTime,
            String staffName) {

        // Check if appointment notifications are enabled
        if (!isNotificationEnabled("APPOINTMENT")) {
            log.info("⏭️ Appointment notifications disabled, skipping");
            return;
        }

        String doctorNumber = getDoctorNumber();
        String formattedDateTime = formatPrettyDateTime(appointmentDateTime);

        String message =
                businessName + "\n\n" +
                customerName + " has booked an appointment on " +
                formattedDateTime + ".\n\n" +
                "Customer Contact Number " + customerPhone + ".\n\n\n" +
                "Thank You\n" +
                "Team CoreConnect 7489660550\n" +
                "https://wa.me/7489660550?text=hi";

        log.info("📱 Sending appointment alert to doctor: {}", doctorNumber);
        sendMessage(doctorNumber, message);
    }

    private String formatPrettyDateTime(String dateTimeStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("MMM dd yyyy 'at' hh:mm a");

            return dateTime.format(formatter);

        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    private static final int ITEM_COL = 13;
    private static final int QTY_COL = 3;

    public void sendBillMessage(BillDetailsResponseDTO bill) {
        
        // Check if bill notifications are enabled
        if (!isNotificationEnabled("BILL")) {
            log.info("⏭️ Bill notifications disabled, skipping doctor notification");
        } else {
            // Send to doctor as well
            String doctorNumber = getDoctorNumber();
            log.info("📱 Sending bill notification to doctor: {}", doctorNumber);
            String message = buildBillMessage(bill);
            sendMessage(doctorNumber, message);
        }
        
        // Always send to customer
        String message = buildBillMessage(bill);
        sendMessage(bill.getPhoneNumber(), message);
    }

    private String buildBillMessage(BillDetailsResponseDTO bill) {

        StringBuilder sb = new StringBuilder();

        sb.append("🧾 *Invoice*\n\n");

        sb.append("📄 No: ").append(bill.getBillNumber()).append("\n");
        sb.append("📅 ").append(formatBillDate(bill.getBillDate())).append("\n\n");

        sb.append("👤 ").append(capitalize(bill.getCustomerName())).append("\n");
        sb.append("📞 ").append(bill.getPhoneNumber()).append("\n\n");

        sb.append("🛍️ *Services*\n");
        sb.append("Item           | Qty | Amount\n");
        sb.append("⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯\n");

        for (var item : bill.getItems()) {

            String itemName = trim(item.getName(), ITEM_COL);

            sb.append(padRight(itemName, ITEM_COL))
              .append(" | ")
              .append(padLeft(String.valueOf(item.getQuantity()), QTY_COL))
              .append(" | ₹")
              .append(Math.round(item.getQuantity() * item.getRate()))
              .append("\n");
        }

        sb.append("⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯⋯\n");

        sb.append("\n💰 *Summary*\n");
        sb.append("Total: ₹").append(bill.getNetPayable())
          .append(" | Discount: ₹").append(bill.getDiscount())
          .append(" | GST: ₹").append(bill.getGst()).append("\n");
        sb.append("——————————\n");

        if (bill.getPayment() != null) {
            sb.append("💳 Paid: ");
            if (bill.getPayment().getCashAmount() > 0)
                sb.append("Cash ₹").append(bill.getPayment().getCashAmount()).append(" ");
            if (bill.getPayment().getCardAmount() > 0)
                sb.append("Card ₹").append(bill.getPayment().getCardAmount()).append(" ");
            if (bill.getPayment().getOtherAmount() > 0)
                sb.append("Other ₹").append(bill.getPayment().getOtherAmount());
            if (bill.getPayment().getAmountToBeCollected() > 0)
                sb.append("Amount to be collected ₹").append(bill.getPayment().getAmountToBeCollected()).append(" ");
            sb.append("\n\n");
        }

        sb.append("🙏 Thank you for visiting!\n");
        sb.append("_Sent via CoreConnect_");

        return sb.toString();
    }

    private String trim(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 1);
    }

    private String padRight(String text, int size) {
        return String.format("%-" + size + "s", text);
    }

    private String padLeft(String text, int size) {
        return String.format("%" + size + "s", text);
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private String formatBillDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' h:mm a");

        return dateTime.format(formatter).toLowerCase();
    }
}