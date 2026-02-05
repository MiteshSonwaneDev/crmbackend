package com.example.crm_system.controller;

import com.example.crm_system.dto.*;
import com.example.crm_system.entity.CustomerPackage;
import com.example.crm_system.entity.Lead;
import com.example.crm_system.entity.LeadStatus;
import com.example.crm_system.service.CustomerPackageService;
import com.example.crm_system.service.LeadService;
import com.example.crm_system.service.PackageService;
import com.example.crm_system.service.PaymentService;
import com.example.crm_system.service.SessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@CrossOrigin(origins = "*")

@RequestMapping("/api/payment")  // ✅ CORRECT - Must be /api/payments
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Add a payment to a customer package
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> addPayment(@Valid @RequestBody AddPaymentRequest request) {
        PaymentResponse response = paymentService.addPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get payment history for a customer package OR customer
     * GET /api/payments?customerPackageId=1
     * GET /api/payments?customerId=1
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(
            @RequestParam(required = false) Long customerPackageId,
            @RequestParam(required = false) Long customerId) {
        
        List<PaymentResponse> response;
        if (customerPackageId != null) {
            response = paymentService.getPaymentHistory(customerPackageId);
        } else if (customerId != null) {
            response = paymentService.getCustomerPaymentHistory(customerId);
        } else {
            throw new IllegalArgumentException("Either customerPackageId or customerId is required");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by ID
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }


    
}