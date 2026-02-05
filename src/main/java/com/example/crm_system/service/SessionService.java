package com.example.crm_system.service;

import com.example.crm_system.dto.AssignPackageRequest;
import com.example.crm_system.dto.CreatePackageRequest;
import com.example.crm_system.dto.CustomerPackageDashboard;
import com.example.crm_system.dto.CustomerPackageResponse;
import com.example.crm_system.dto.ExecuteSessionRequest;
import com.example.crm_system.dto.PackageResponse;
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
public class SessionService {

    private final PackageSessionRepository sessionRepository;
    private final CustomerPackageRepository customerPackageRepository;
    private final SessionProductConsumptionRepository consumptionRepository;
    private final ServiceRepository serviceRepository;
    private final ProductRepository productRepository;

    /**
     * Execute a session for a customer package
     * This is the core workflow for each clinic visit
     */
    public SessionResponse executeSession(ExecuteSessionRequest request) {
        log.info("Executing session for customer package: {}", request.getCustomerPackageId());
        
        // Load and validate customer package
        CustomerPackage customerPackage = customerPackageRepository
            .findById(request.getCustomerPackageId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer package not found"));
        
        // Validate package can be used
        validatePackageForSession(customerPackage);
        
        // Get next session number
        Integer nextSessionNumber = getNextSessionNumber(request.getCustomerPackageId());
        
        // Create session record
        PackageSession session = PackageSession.builder()
            .customerPackage(customerPackage)
            .sessionDate(request.getSessionDate())
            .sessionNumber(nextSessionNumber)
            .performedByStaff(request.getPerformedByStaff())
            .notes(request.getNotes())
            .build();
        
        // Add service records
        if (request.getServices() != null && !request.getServices().isEmpty()) {
            for (SessionServiceRequest svcReq : request.getServices()) {
                ServiceEntity service = serviceRepository.findById(svcReq.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + svcReq.getServiceId()));
                
                SessionServiceRecord record = SessionServiceRecord.builder()
                    .service(service)
                    .performedBy(svcReq.getPerformedBy())
                    .serviceNotes(svcReq.getServiceNotes())
                    .completed(true)
                    .build();
                
                session.addServiceRecord(record);
            }
        }
        
        // Add product consumption and deduct inventory
        if (request.getProducts() != null && !request.getProducts().isEmpty()) {
            for (SessionProductRequest prodReq : request.getProducts()) {
                ProductEntity product = productRepository.findById(prodReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + prodReq.getProductId()));
                
                // Validate stock availability if deduction is requested
                if (prodReq.getDeductFromInventory()) {
                    validateProductStock(product, prodReq.getQuantityConsumed());
                    deductProductStock(product, prodReq.getQuantityConsumed());
                }
                
                SessionProductConsumption consumption = SessionProductConsumption.builder()
                    .product(product)
                    .quantityConsumed(prodReq.getQuantityConsumed())
                    .deductedFromInventory(prodReq.getDeductFromInventory())
                    .consumptionNotes(prodReq.getConsumptionNotes())
                    .build();
                
                session.addProductConsumption(consumption);
            }
        }
        
        // Save session
        PackageSession savedSession = sessionRepository.save(session);
        
        // Update customer package - reduce remaining sessions
        customerPackage.setRemainingSessions(customerPackage.getRemainingSessions() - 1);
        
        // Auto-complete package if no sessions remaining
        if (customerPackage.getRemainingSessions() == 0) {
            customerPackage.setStatus(CustomerPackage.PackageStatus.COMPLETED);
            log.info("Customer package {} marked as COMPLETED", customerPackage.getId());
        }
        
        customerPackageRepository.save(customerPackage);
        
        log.info("Session executed successfully with ID: {}", savedSession.getId());
        
        return mapToSessionResponse(savedSession);
    }

    /**
     * Get session history for a customer package
     */
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionHistory(Long customerPackageId) {
        List<PackageSession> sessions = sessionRepository
            .findByCustomerPackageIdOrderBySessionDateDesc(customerPackageId);
        
        return sessions.stream()
            .map(this::mapToSessionResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get all sessions for a customer across all packages
     */
    @Transactional(readOnly = true)
    public List<SessionResponse> getCustomerSessionHistory(Long customerId) {
        List<PackageSession> sessions = sessionRepository
            .findByCustomerPackageCustomerIdOrderBySessionDateDesc(customerId);
        
        return sessions.stream()
            .map(this::mapToSessionResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get session by ID with full details
     */
    @Transactional(readOnly = true)
    public SessionResponse getSessionById(Long sessionId) {
        PackageSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        
        return mapToSessionResponse(session);
    }

    /**
     * Update session notes/feedback
     */
    public SessionResponse updateSessionNotes(Long sessionId, String notes, String customerFeedback) {
        PackageSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        
        if (notes != null) {
            session.setNotes(notes);
        }
        if (customerFeedback != null) {
            session.setCustomerFeedback(customerFeedback);
        }
        
        PackageSession updated = sessionRepository.save(session);
        log.info("Session {} notes updated", sessionId);
        
        return mapToSessionResponse(updated);
    }

    // Private helper methods

    /**
     * Validate if package can be used for a session
     */
    private void validatePackageForSession(CustomerPackage customerPackage) {
        // Check status
        if (customerPackage.getStatus() != CustomerPackage.PackageStatus.ACTIVE) {
            throw new BusinessException("Package is not active. Current status: " + customerPackage.getStatus());
        }
        
        // Check remaining sessions
        if (customerPackage.getRemainingSessions() <= 0) {
            throw new BusinessException("No remaining sessions in this package");
        }
        
        // Check expiry
        if (customerPackage.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new BusinessException("Package has expired on " + customerPackage.getExpiryDate());
        }
    }

    /**
     * Get next session number for a package
     */
    private Integer getNextSessionNumber(Long customerPackageId) {
        Integer maxSessionNumber = sessionRepository.findMaxSessionNumber(customerPackageId);
        return maxSessionNumber == null ? 1 : maxSessionNumber + 1;
    }

    /**
     * Validate product stock before consumption
     */
    private void validateProductStock(ProductEntity product, BigDecimal requiredQuantity) {
    // ✅ FIX: Use quantity field instead of minStockQuantity
    Integer stockInt = product.getQuantity();

    BigDecimal availableStock = stockInt == null 
            ? BigDecimal.ZERO 
            : BigDecimal.valueOf(stockInt);

    if (availableStock.compareTo(requiredQuantity) < 0) {
        throw new BusinessException(
            String.format(
                "Insufficient stock for product '%s'. Required: %s, Available: %s",
                product.getProductName(), requiredQuantity, availableStock
            )
        );
    }
}

    /**
     * Deduct product from inventory
     */
 private void deductProductStock(ProductEntity product, BigDecimal quantity) {
    // ✅ FIX: Use quantity field instead of minStockQuantity
    Integer currentStockInt = product.getQuantity();

    BigDecimal currentStock = currentStockInt == null
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(currentStockInt);

    BigDecimal newStock = currentStock.subtract(quantity);

    if (newStock.compareTo(BigDecimal.ZERO) < 0) {
        throw new BusinessException("Stock cannot be negative for product: " + product.getProductName());
    }

    // ✅ FIX: Update quantity field instead of minStockQuantity
    product.setQuantity(newStock.intValue());

    productRepository.save(product);

    log.info("Deducted {} units of product {} from inventory. New stock: {}",
            quantity, product.getProductName(), newStock);
}

    /**
     * Map session entity to response DTO
     */
   private SessionResponse mapToSessionResponse(PackageSession session) {

    List<ServicePerformedResponse> services =
            session.getServicesPerformed() == null
                    ? List.of()
                    : session.getServicesPerformed().stream()
                        .map((SessionServiceRecord s) -> ServicePerformedResponse.builder()
                                .serviceId(s.getService().getId())
                                .serviceName(s.getService().getServiceName()) // ✅ fixed
                                .performedBy(s.getPerformedBy())
                                .serviceNotes(s.getServiceNotes())
                                .build())
                        .collect(Collectors.toList());

    List<ProductConsumedResponse> products =
            session.getProductsConsumed() == null
                    ? List.of()
                    : session.getProductsConsumed().stream()
                        .map((SessionProductConsumption p) -> ProductConsumedResponse.builder()
                                .productId(p.getProduct().getId())
                                .productName(p.getProduct().getProductName()) // ✅ fixed
                                .quantityConsumed(p.getQuantityConsumed())
                                .deductedFromInventory(p.getDeductedFromInventory())
                                .consumptionNotes(p.getConsumptionNotes())
                                .build())
                        .collect(Collectors.toList());

    return SessionResponse.builder()
            .id(session.getId())
            .customerPackageId(session.getCustomerPackage().getId())
            .sessionDate(session.getSessionDate())
            .sessionNumber(session.getSessionNumber())
            .performedByStaff(session.getPerformedByStaff())
            .notes(session.getNotes())
            .servicesPerformed(services)
            .productsConsumed(products)
            .createdAt(session.getCreatedAt())
            .build();
}

}