package com.example.crm_system.service;

import com.example.crm_system.dto.AssignPackageRequest;
import com.example.crm_system.dto.CreatePackageRequest;
import com.example.crm_system.dto.CustomerPackageDashboard;
import com.example.crm_system.dto.CustomerPackageResponse;
import com.example.crm_system.dto.PackageResponse;
import com.example.crm_system.dto.PaymentSummary;
import com.example.crm_system.dto.ProductMappingRequest;
import com.example.crm_system.dto.ProductMappingResponse;
import com.example.crm_system.dto.ProgressStats;
import com.example.crm_system.dto.ServiceMappingRequest;
import com.example.crm_system.dto.ServiceMappingResponse;
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
public class CustomerPackageService {

    private final CustomerPackageRepository customerPackageRepository;
    private final PackageRepository packageRepository;
    private final CustomerRepository customerRepository;
    private final PackageSessionRepository sessionRepository;
    private final PackagePaymentRepository paymentRepository;

    /**
     * Assign a package to a customer
     */
    public CustomerPackageResponse assignPackageToCustomer(AssignPackageRequest request) {
        log.info("Assigning package {} to customer {}", request.getPackageId(), request.getCustomerId());
        
        // Validate customer exists
        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        
        // Validate package exists and is active
        PackageEntity packageEntity = packageRepository.findById(request.getPackageId())
            .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        
        if (!packageEntity.getActive()) {
            throw new BusinessException("Cannot assign inactive package");
        }
        
        // Calculate expiry date
        LocalDate expiryDate = request.getStartDate().plusDays(packageEntity.getValidityInDays());
        
        // Create customer package
        CustomerPackage customerPackage = CustomerPackage.builder()
            .customer(customer)
            .packageEntity(packageEntity)
            .startDate(request.getStartDate())
            .expiryDate(expiryDate)
            .totalSessions(packageEntity.getTotalSessions())
            .remainingSessions(packageEntity.getTotalSessions())
            .totalAmount(packageEntity.getFinalPrice())
            .paidAmount(BigDecimal.ZERO)
            .dueAmount(packageEntity.getFinalPrice())
            .status(CustomerPackage.PackageStatus.ACTIVE)
            .notes(request.getNotes())
            .build();
        
        CustomerPackage saved = customerPackageRepository.save(customerPackage);
        log.info("Package assigned successfully with ID: {}", saved.getId());
        
        return mapToCustomerPackageResponse(saved);
    }

    /**
     * Get customer package details with full dashboard
     */
    @Transactional(readOnly = true)
    public CustomerPackageDashboard getCustomerPackageDashboard(Long customerPackageId) {
        CustomerPackage customerPackage = customerPackageRepository.findById(customerPackageId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer package not found"));
        
        // Get recent sessions
        List<PackageSession> sessions = sessionRepository
            .findByCustomerPackageIdOrderBySessionDateDesc(customerPackageId);
        
        List<SessionSummary> sessionSummaries = sessions.stream()
            .limit(10) // Latest 10 sessions
            .map(s -> SessionSummary.builder()
                .id(s.getId())
                .sessionDate(s.getSessionDate())
                .sessionNumber(s.getSessionNumber())
                .performedByStaff(s.getPerformedByStaff())
                .servicesCount(s.getServicesPerformed().size())
                .productsCount(s.getProductsConsumed().size())
                .build())
            .collect(Collectors.toList());
        
        // Get payment history
        List<PackagePayment> payments = paymentRepository
            .findByCustomerPackageIdOrderByPaymentDateDesc(customerPackageId);
        
        List<PaymentSummary> paymentSummaries = payments.stream()
            .map(p -> PaymentSummary.builder()
                .id(p.getId())
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .paymentMethod(p.getPaymentMethod())
                .invoiceNumber(p.getInvoiceNumber())
                .build())
            .collect(Collectors.toList());
        
        // Calculate progress stats
        Integer completedSessions = customerPackage.getTotalSessions() - customerPackage.getRemainingSessions();
        BigDecimal completionPercentage = BigDecimal.valueOf(completedSessions)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(customerPackage.getTotalSessions()), 2, RoundingMode.HALF_UP);
        
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), customerPackage.getExpiryDate());
        boolean isExpired = LocalDate.now().isAfter(customerPackage.getExpiryDate());
        
        ProgressStats progressStats = ProgressStats.builder()
            .totalSessions(customerPackage.getTotalSessions())
            .completedSessions(completedSessions)
            .remainingSessions(customerPackage.getRemainingSessions())
            .completionPercentage(completionPercentage)
            .daysRemaining((int) daysRemaining)
            .isExpired(isExpired)
            .build();
        
        return CustomerPackageDashboard.builder()
            .packageDetails(mapToCustomerPackageResponse(customerPackage))
            .recentSessions(sessionSummaries)
            .paymentHistory(paymentSummaries)
            .progressStats(progressStats)
            .build();
    }

    /**
     * Get all packages for a customer
     */
    @Transactional(readOnly = true)
    public List<CustomerPackageResponse> getCustomerPackages(Long customerId, CustomerPackage.PackageStatus status) {
        List<CustomerPackage> packages = status != null 
            ? customerPackageRepository.findByCustomerIdAndStatus(customerId, status)
            : customerPackageRepository.findByCustomerId(customerId);
        
        return packages.stream()
            .map(this::mapToCustomerPackageResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get active packages for a customer (not expired, has remaining sessions)
     */
    @Transactional(readOnly = true)
    public List<CustomerPackageResponse> getActiveCustomerPackages(Long customerId) {
        List<CustomerPackage> packages = customerPackageRepository
            .findActivePackagesForCustomer(customerId, LocalDate.now());
        
        return packages.stream()
            .map(this::mapToCustomerPackageResponse)
            .collect(Collectors.toList());
    }

    /**
     * Update customer package status
     */
    public CustomerPackageResponse updatePackageStatus(Long customerPackageId, 
                                                       CustomerPackage.PackageStatus newStatus) {
        CustomerPackage customerPackage = customerPackageRepository.findById(customerPackageId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer package not found"));
        
        // Validate status transition
        validateStatusTransition(customerPackage.getStatus(), newStatus);
        
        customerPackage.setStatus(newStatus);
        CustomerPackage updated = customerPackageRepository.save(customerPackage);
        
        log.info("Customer package {} status updated to {}", customerPackageId, newStatus);
        
        return mapToCustomerPackageResponse(updated);
    }

    /**
     * Extend package validity
     */
    public CustomerPackageResponse extendPackageValidity(Long customerPackageId, Integer additionalDays) {
        CustomerPackage customerPackage = customerPackageRepository.findById(customerPackageId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer package not found"));
        
        if (additionalDays <= 0) {
            throw new BusinessException("Additional days must be positive");
        }
        
        LocalDate newExpiryDate = customerPackage.getExpiryDate().plusDays(additionalDays);
        customerPackage.setExpiryDate(newExpiryDate);
        
        // If package was expired, reactivate it
        if (customerPackage.getStatus() == CustomerPackage.PackageStatus.EXPIRED 
            && customerPackage.getRemainingSessions() > 0) {
            customerPackage.setStatus(CustomerPackage.PackageStatus.ACTIVE);
        }
        
        CustomerPackage updated = customerPackageRepository.save(customerPackage);
        log.info("Package {} validity extended by {} days", customerPackageId, additionalDays);
        
        return mapToCustomerPackageResponse(updated);
    }

    // Helper methods
    private void validateStatusTransition(CustomerPackage.PackageStatus currentStatus, 
                                         CustomerPackage.PackageStatus newStatus) {
        // Define valid transitions
        if (currentStatus == CustomerPackage.PackageStatus.CANCELLED) {
            throw new BusinessException("Cannot change status of cancelled package");
        }
        
        if (currentStatus == CustomerPackage.PackageStatus.COMPLETED && 
            newStatus != CustomerPackage.PackageStatus.CANCELLED) {
            throw new BusinessException("Completed package can only be cancelled");
        }
    }

    private CustomerPackageResponse mapToCustomerPackageResponse(CustomerPackage entity) {
        Integer completedSessions = entity.getTotalSessions() - entity.getRemainingSessions();
        
        return CustomerPackageResponse.builder()
            .id(entity.getId())
            .customerId(entity.getCustomer().getId())
            .customerName(entity.getCustomer().getCustomerName()) // Assuming Customer has getName()
            .packageId(entity.getPackageEntity().getId())
            .packageName(entity.getPackageEntity().getPackageName())
            .startDate(entity.getStartDate())
            .expiryDate(entity.getExpiryDate())
            .totalSessions(entity.getTotalSessions())
            .remainingSessions(entity.getRemainingSessions())
            .completedSessions(completedSessions)
            .totalAmount(entity.getTotalAmount())
            .paidAmount(entity.getPaidAmount())
            .dueAmount(entity.getDueAmount())
            .status(entity.getStatus())
            .notes(entity.getNotes())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}

// Custom exception for business logic violations
class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}