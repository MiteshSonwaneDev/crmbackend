package com.example.crm_system.service;

import com.example.crm_system.dto.AddPaymentRequest;
import com.example.crm_system.dto.AssignPackageRequest;
import com.example.crm_system.dto.CreatePackageRequest;
import com.example.crm_system.dto.CustomerPackageDashboard;
import com.example.crm_system.dto.CustomerPackageResponse;
import com.example.crm_system.dto.ExecuteSessionRequest;
import com.example.crm_system.dto.PackageResponse;
import com.example.crm_system.dto.PaymentResponse;
import com.example.crm_system.dto.PaymentSummary;
import com.example.crm_system.dto.ProductConsumedResponse;
import com.example.crm_system.dto.ProductMappingRequest;
import com.example.crm_system.dto.ProductMappingResponse;
import com.example.crm_system.dto.ProgressStats;
import com.example.crm_system.dto.ServiceMappingRequest;
import com.example.crm_system.dto.ServiceMappingResponse;
import com.example.crm_system.dto.ServicePerformedResponse;
import com.example.crm_system.dto.SessionProductRequest;
import com.example.crm_system.dto.SessionResponse;
import com.example.crm_system.dto.SessionServiceRequest;
import com.example.crm_system.dto.SessionSummary;
import com.example.crm_system.entity.Business;
import com.example.crm_system.entity.CustomerEntity;
import com.example.crm_system.entity.CustomerPackage;
import com.example.crm_system.entity.PackageEntity;
import com.example.crm_system.entity.PackagePayment;
import com.example.crm_system.entity.PackageProductMapping;
import com.example.crm_system.entity.PackageServiceMapping;
import com.example.crm_system.entity.PackageSession;
import com.example.crm_system.entity.ProductEntity;
import com.example.crm_system.entity.ServiceEntity;
import com.example.crm_system.entity.SessionProductConsumption;
import com.example.crm_system.entity.SessionServiceRecord;
import com.example.crm_system.repository.BusinessRepository;
import com.example.crm_system.repository.CustomerPackageRepository;
import com.example.crm_system.repository.CustomerRepository;
import com.example.crm_system.repository.PackagePaymentRepository;
import com.example.crm_system.repository.PackageRepository;
import com.example.crm_system.repository.PackageSessionRepository;
import com.example.crm_system.repository.ProductRepository;
import com.example.crm_system.repository.ServiceRepository;
import com.example.crm_system.repository.SessionProductConsumptionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PackagePaymentRepository paymentRepository;
    private final CustomerPackageRepository customerPackageRepository;

    /**
     * Add a payment to a customer package
     * Supports both full and partial payments
     */
    public PaymentResponse addPayment(AddPaymentRequest request) {
        log.info("Adding payment of {} for customer package {}", 
            request.getAmount(), request.getCustomerPackageId());
        
        // Load customer package
        CustomerPackage customerPackage = customerPackageRepository
            .findById(request.getCustomerPackageId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer package not found"));
        
        // Validate payment amount
        validatePaymentAmount(customerPackage, request.getAmount());
        
        // Create payment record
        PackagePayment payment = PackagePayment.builder()
            .customerPackage(customerPackage)
            .amount(request.getAmount())
            .paymentDate(request.getPaymentDate())
            .paymentMethod(request.getPaymentMethod())
            .transactionReference(request.getTransactionReference())
            .invoiceNumber(request.getInvoiceNumber())
            .notes(request.getNotes())
            .receivedBy(request.getReceivedBy())
            .build();
        
        PackagePayment savedPayment = paymentRepository.save(payment);
        
        // Update customer package paid and due amounts
        customerPackage.setPaidAmount(customerPackage.getPaidAmount().add(request.getAmount()));
        customerPackage.calculateDueAmount();
        customerPackageRepository.save(customerPackage);
        
        log.info("Payment recorded successfully with ID: {}. Remaining due: {}", 
            savedPayment.getId(), customerPackage.getDueAmount());
        
        return mapToPaymentResponse(savedPayment);
    }

    /**
     * Get payment history for a customer package
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(Long customerPackageId) {
        List<PackagePayment> payments = paymentRepository
            .findByCustomerPackageIdOrderByPaymentDateDesc(customerPackageId);
        
        return payments.stream()
            .map(this::mapToPaymentResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get all payments for a customer across all packages
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getCustomerPaymentHistory(Long customerId) {
        List<PackagePayment> payments = paymentRepository
            .findByCustomerPackageCustomerIdOrderByPaymentDateDesc(customerId);
        
        return payments.stream()
            .map(this::mapToPaymentResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        PackagePayment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        return mapToPaymentResponse(payment);
    }

    /**
     * Get payment summary for a package
     */
    @Transactional(readOnly = true)
    public PaymentSummaryReport getPaymentSummary(Long customerPackageId) {
        CustomerPackage customerPackage = customerPackageRepository.findById(customerPackageId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer package not found"));
        
        List<PackagePayment> payments = paymentRepository
            .findByCustomerPackageIdOrderByPaymentDateDesc(customerPackageId);
        
        // Calculate payment statistics
        int totalPayments = payments.size();
        BigDecimal totalPaid = payments.stream()
            .map(PackagePayment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return PaymentSummaryReport.builder()
            .customerPackageId(customerPackageId)
            .totalAmount(customerPackage.getTotalAmount())
            .totalPaid(totalPaid)
            .dueAmount(customerPackage.getDueAmount())
            .numberOfPayments(totalPayments)
            .isFullyPaid(customerPackage.getDueAmount().compareTo(BigDecimal.ZERO) == 0)
            .payments(payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList()))
            .build();
    }

    /**
     * Update payment details (e.g., add invoice number later)
     */
    public PaymentResponse updatePayment(Long paymentId, UpdatePaymentRequest request) {
        PackagePayment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        if (request.getInvoiceNumber() != null) {
            payment.setInvoiceNumber(request.getInvoiceNumber());
        }
        if (request.getTransactionReference() != null) {
            payment.setTransactionReference(request.getTransactionReference());
        }
        if (request.getNotes() != null) {
            payment.setNotes(request.getNotes());
        }
        
        PackagePayment updated = paymentRepository.save(payment);
        log.info("Payment {} updated", paymentId);
        
        return mapToPaymentResponse(updated);
    }

    // Private helper methods

    /**
     * Validate payment amount doesn't exceed due amount
     */
    private void validatePaymentAmount(CustomerPackage customerPackage, BigDecimal paymentAmount) {
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be greater than zero");
        }
        
        BigDecimal dueAmount = customerPackage.getDueAmount();
        if (paymentAmount.compareTo(dueAmount) > 0) {
            throw new BusinessException(
                String.format("Payment amount (%.2f) exceeds due amount (%.2f)", 
                    paymentAmount, dueAmount)
            );
        }
    }

    /**
     * Map payment entity to response DTO
     */
    private PaymentResponse mapToPaymentResponse(PackagePayment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .customerPackageId(payment.getCustomerPackage().getId())
            .amount(payment.getAmount())
            .paymentDate(payment.getPaymentDate())
            .paymentMethod(payment.getPaymentMethod())
            .transactionReference(payment.getTransactionReference())
            .invoiceNumber(payment.getInvoiceNumber())
            .notes(payment.getNotes())
            .receivedBy(payment.getReceivedBy())
            .createdAt(payment.getCreatedAt())
            .build();
    }
}

// Additional DTOs for payment service

@lombok.Data
@lombok.Builder
class PaymentSummaryReport {
    private Long customerPackageId;
    private BigDecimal totalAmount;
    private BigDecimal totalPaid;
    private BigDecimal dueAmount;
    private Integer numberOfPayments;
    private Boolean isFullyPaid;
    private List<PaymentResponse> payments;
}

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class UpdatePaymentRequest {
    private String invoiceNumber;
    private String transactionReference;
    private String notes;
}