package com.example.crm_system.controller;
import com.example.crm_system.dto.BillDetailsResponseDTO;
import com.example.crm_system.dto.BillPaymentResponseDTO;
import com.example.crm_system.dto.BillWithPaymentDTO;
import com.example.crm_system.dto.CustomerPaymentSummary;
import com.example.crm_system.entity.BillEntity;
import com.example.crm_system.entity.BillPayment;
import com.example.crm_system.entity.BillSettlement;
import com.example.crm_system.repository.BillSettlementRepository;
import com.example.crm_system.service.BillPaymentService;
import com.example.crm_system.service.WhatsAppService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")

@CrossOrigin(origins = "*")
public class BillPaymentController {

    private final BillPaymentService paymentService;
    private final BillSettlementRepository billSettlementRepository;
    private final WhatsAppService whatsAppService;



    public BillPaymentController(BillPaymentService paymentService,BillSettlementRepository billSettlementRepository,WhatsAppService whatsAppService) {
            this.paymentService = paymentService;
            this.billSettlementRepository = billSettlementRepository;
            this.whatsAppService =whatsAppService;
        }

        // ✅ Save payment for a bill
        @PostMapping("/{billId}")
        public BillPayment savePayment(@PathVariable Long billId, @RequestBody BillPayment payment) {
            return paymentService.recordPayment(billId, payment);
        }

    // ✅ Get payment details for a bill
    @GetMapping("/{billId}")
    public BillPayment getPaymentDetails(@PathVariable Long billId) {
        
        return paymentService.getPaymentDetails(billId);
    }
    @PutMapping("/{paymentId}")
public BillPayment updatePayment(@PathVariable Long paymentId, @RequestBody BillPayment updatedPayment) {
    return paymentService.updatePayment(paymentId, updatedPayment);
}
@GetMapping("/business/{businessId}/other-amount")
public List<BillWithPaymentDTO> getOtherAmountBills(
        @PathVariable Long businessId
) {
    return paymentService.getOtherAmountBillsByBusiness(businessId);
}


   @GetMapping("/business/{businessId}")
public List<BillPaymentResponseDTO> getPaymentsByBusiness(@PathVariable Long businessId) {
    return paymentService.getPaymentsByBusiness(businessId);
}

    @GetMapping("/customer/{businessId}/{phoneNumber}")
public CustomerPaymentSummary getPaymentsByCustomer(
        @PathVariable Long businessId,
        @PathVariable String phoneNumber) {
    return paymentService.getPaymentsByCustomerPhone(businessId, phoneNumber);
}

// ✅ Get full bill details by businessId and billId
@GetMapping("/business/{businessId}/bill/{billId}")
public BillDetailsResponseDTO getBillDetails(
        @PathVariable Long businessId,
        @PathVariable Long billId) {
    return paymentService.getBillDetails(businessId, billId);
}

 @PostMapping("/send-bill/{businessId}/{billId}")
    public ResponseEntity<String> sendBillOnWhatsApp(
            @PathVariable Long businessId,
            @PathVariable Long billId) {

        BillDetailsResponseDTO bill =
                paymentService.getBillDetails(businessId, billId);

        whatsAppService.sendBillMessage(bill);

        return ResponseEntity.ok("WhatsApp bill sent");
    }
@PostMapping("/business/{businessId}/bill/{billId}/settle")
public ResponseEntity<Map<String, Object>> settleBill(
        @PathVariable Long businessId,
        @PathVariable Long billId) {

    paymentService.settleBill(businessId, billId);

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "Bill settled successfully");
    response.put("billId", billId);

    return ResponseEntity.ok(response); // ✅ RETURN JSON
}

@GetMapping("/business/{businessId}/bill/{billId}/settlement")
public BillSettlement getSettlement(
        @PathVariable Long businessId,
        @PathVariable Long billId) {

    return billSettlementRepository
            .findByBusinessIdAndBillId(businessId, billId)
            .orElseThrow(() -> new RuntimeException("Settlement not found"));
}
@PostMapping("/business/{businessId}/bill/{billId}/upload-invoice")
public ResponseEntity<?> uploadInvoiceImage(
        @PathVariable Long businessId,
        @PathVariable Long billId,
        @RequestBody String base64Image) {

    boolean saved = paymentService.saveInvoiceImage(
            businessId,
            billId,
            base64Image
    );

    return saved
            ? ResponseEntity.ok("Invoice image uploaded successfully")
            : ResponseEntity.badRequest().body("Failed to upload invoice image");
}

@GetMapping(
        value = "/business/{businessId}/bill/{billId}/invoice",
        produces = MediaType.IMAGE_PNG_VALUE
)
public ResponseEntity<byte[]> getInvoiceImage(
        @PathVariable Long businessId,
        @PathVariable Long billId
) {

    byte[] imageBytes = paymentService.getInvoiceImage(businessId, billId);

    if (imageBytes == null) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice.png")
            .contentType(MediaType.IMAGE_PNG)
            .body(imageBytes);
}


}
