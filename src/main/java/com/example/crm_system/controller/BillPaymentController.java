package com.example.crm_system.controller;
import com.example.crm_system.dto.BillDetailsResponseDTO;
import com.example.crm_system.dto.BillPaymentResponseDTO;
import com.example.crm_system.dto.CustomerPaymentSummary;
import com.example.crm_system.entity.BillPayment;
import com.example.crm_system.service.BillPaymentService;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class BillPaymentController {

    private final BillPaymentService paymentService;

    public BillPaymentController(BillPaymentService paymentService) {
            this.paymentService = paymentService;
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

}
