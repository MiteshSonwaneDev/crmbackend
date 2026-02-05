package com.example.crm_system.service;

import com.example.crm_system.dto.CreatePackageRequest;
import com.example.crm_system.dto.PackageResponse;
import com.example.crm_system.dto.ProductMappingRequest;
import com.example.crm_system.dto.ProductMappingResponse;
import com.example.crm_system.dto.ServiceMappingRequest;
import com.example.crm_system.dto.ServiceMappingResponse;
import com.example.crm_system.entity.Business;
import com.example.crm_system.entity.PackageEntity;
import com.example.crm_system.entity.PackageProductMapping;
import com.example.crm_system.entity.PackageServiceMapping;
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
public class PackageService {

    private final PackageRepository packageRepository;
    private final CustomerPackageRepository customerPackageRepository;
    private final PackageSessionRepository sessionRepository;
    private final PackagePaymentRepository paymentRepository;
    private final SessionProductConsumptionRepository consumptionRepository;
    
    // Assuming these exist in your system
    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final ProductRepository productRepository;

    /**
     * Create a new package template
     */
    public PackageResponse createPackage(CreatePackageRequest request) {
        log.info("Creating new package: {}", request.getPackageName());
        
        // Validate business exists
        Business business = businessRepository.findById(request.getBusinessId())
            .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        
        // Build package entity
        PackageEntity packageEntity = PackageEntity.builder()
            .business(business)
            .packageName(request.getPackageName())
            .description(request.getDescription())
            .totalSessions(request.getTotalSessions())
            .validityInDays(request.getValidityInDays())
            .originalPrice(request.getOriginalPrice())
            .discountType(request.getDiscountType())
            .discountValue(request.getDiscountValue())
            .gst(request.getGst())
            .active(request.getActive())
            .build();
        
        // Add service mappings
        if (request.getServices() != null) {
            for (ServiceMappingRequest svcReq : request.getServices()) {
                ServiceEntity service = serviceRepository.findById(svcReq.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + svcReq.getServiceId()));
                
                PackageServiceMapping mapping = PackageServiceMapping.builder()
                    .service(service)
                    .sessionsForService(svcReq.getSessionsForService())
                    .pricePerSession(svcReq.getPricePerSession())
                    .staffPreference(svcReq.getStaffPreference())
                    .notes(svcReq.getNotes())
                    .build();
                
                packageEntity.addServiceMapping(mapping);
            }
        }
        
        // Add product mappings
        if (request.getProducts() != null) {
            for (ProductMappingRequest prodReq : request.getProducts()) {
                ProductEntity product = productRepository.findById(prodReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + prodReq.getProductId()));
                
                PackageProductMapping mapping = PackageProductMapping.builder()
                    .product(product)
                    .quantityPerPackage(prodReq.getQuantityPerPackage())
                    .usagePerSession(prodReq.getUsagePerSession())
                    .internalConsumptionOnly(prodReq.getInternalConsumptionOnly())
                    .usageInstructions(prodReq.getUsageInstructions())
                    .build();
                
                packageEntity.addProductMapping(mapping);
            }
        }
        
        PackageEntity saved = packageRepository.save(packageEntity);
        log.info("Package created successfully with ID: {}", saved.getId());
        
        return mapToPackageResponse(saved);
    }

    /**
     * Update existing package
     */
    public PackageResponse updatePackage(Long packageId, CreatePackageRequest request) {
        log.info("Updating package ID: {}", packageId);
        
        PackageEntity packageEntity = packageRepository.findById(packageId)
            .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        
        // Update basic fields
        packageEntity.setPackageName(request.getPackageName());
        packageEntity.setDescription(request.getDescription());
        packageEntity.setTotalSessions(request.getTotalSessions());
        packageEntity.setValidityInDays(request.getValidityInDays());
        packageEntity.setOriginalPrice(request.getOriginalPrice());
        packageEntity.setDiscountType(request.getDiscountType());
        packageEntity.setDiscountValue(request.getDiscountValue());
        packageEntity.setGst(request.getGst());
        packageEntity.setActive(request.getActive());
        
        // Clear and rebuild service mappings
        packageEntity.getServicesMappings().clear();
        if (request.getServices() != null) {
            for (ServiceMappingRequest svcReq : request.getServices()) {
                ServiceEntity service = serviceRepository.findById(svcReq.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
                
                PackageServiceMapping mapping = PackageServiceMapping.builder()
                    .service(service)
                    .sessionsForService(svcReq.getSessionsForService())
                    .pricePerSession(svcReq.getPricePerSession())
                    .staffPreference(svcReq.getStaffPreference())
                    .notes(svcReq.getNotes())
                    .build();
                
                packageEntity.addServiceMapping(mapping);
            }
        }
        
        // Clear and rebuild product mappings
        packageEntity.getProductsMappings().clear();
        if (request.getProducts() != null) {
            for (ProductMappingRequest prodReq : request.getProducts()) {
                ProductEntity product = productRepository.findById(prodReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                
                PackageProductMapping mapping = PackageProductMapping.builder()
                    .product(product)
                    .quantityPerPackage(prodReq.getQuantityPerPackage())
                    .usagePerSession(prodReq.getUsagePerSession())
                    .internalConsumptionOnly(prodReq.getInternalConsumptionOnly())
                    .usageInstructions(prodReq.getUsageInstructions())
                    .build();
                
                packageEntity.addProductMapping(mapping);
            }
        }
        
        PackageEntity updated = packageRepository.save(packageEntity);
        log.info("Package updated successfully");
        
        return mapToPackageResponse(updated);
    }

    /**
     * Get package by ID
     */
    @Transactional(readOnly = true)
    public PackageResponse getPackageById(Long packageId) {
        PackageEntity packageEntity = packageRepository.findById(packageId)
            .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        
        return mapToPackageResponse(packageEntity);
    }

    /**
     * Get all packages for a business
     */
    @Transactional(readOnly = true)
    public List<PackageResponse> getAllPackages(Long businessId, Boolean activeOnly) {
        List<PackageEntity> packages = activeOnly 
            ? packageRepository.findByBusinessIdAndActiveTrue(businessId)
            : packageRepository.findByBusinessId(businessId);
        
        return packages.stream()
            .map(this::mapToPackageResponse)
            .collect(Collectors.toList());
    }

    /**
     * Delete package (soft delete by marking inactive)
     */
    public void deletePackage(Long packageId) {
        PackageEntity packageEntity = packageRepository.findById(packageId)
            .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        
        packageEntity.setActive(false);
        packageRepository.save(packageEntity);
        
        log.info("Package ID {} marked as inactive", packageId);
    }

    // Helper method to map entity to response
    private PackageResponse mapToPackageResponse(PackageEntity entity) {
        List<ServiceMappingResponse> services = entity.getServicesMappings().stream()
            .map(m -> ServiceMappingResponse.builder()
                .id(m.getId())
                .serviceId(m.getService().getId())
                .serviceName(m.getService().getServiceName()) // Assuming ServiceEntity has getName()
                .sessionsForService(m.getSessionsForService())
                .pricePerSession(m.getPricePerSession())
                .staffPreference(m.getStaffPreference())
                .notes(m.getNotes())
                .build())
            .collect(Collectors.toList());
        
        List<ProductMappingResponse> products = entity.getProductsMappings().stream()
            .map(m -> ProductMappingResponse.builder()
                .id(m.getId())
                .productId(m.getProduct().getId())
                .productName(m.getProduct().getProductName()) // Assuming ProductEntity has getName()
                .quantityPerPackage(m.getQuantityPerPackage())
                .usagePerSession(m.getUsagePerSession())
                .internalConsumptionOnly(m.getInternalConsumptionOnly())
                .usageInstructions(m.getUsageInstructions())
                .build())
            .collect(Collectors.toList());
        
        return PackageResponse.builder()
            .id(entity.getId())
            .businessId(entity.getBusiness().getId())
            .packageName(entity.getPackageName())
            .description(entity.getDescription())
            .totalSessions(entity.getTotalSessions())
            .validityInDays(entity.getValidityInDays())
            .originalPrice(entity.getOriginalPrice())
            .discountType(entity.getDiscountType())
            .discountValue(entity.getDiscountValue())
            .finalPrice(entity.getFinalPrice())
            .gst(entity.getGst())
            .active(entity.getActive())
            .services(services)
            .products(products)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}

// Custom exception class
class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}