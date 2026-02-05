package com.example.crm_system.controller;

import com.example.crm_system.dto.*;
import com.example.crm_system.entity.CustomerPackage;
import com.example.crm_system.entity.Lead;
import com.example.crm_system.entity.LeadStatus;
import com.example.crm_system.service.CustomerPackageService;
import com.example.crm_system.service.LeadService;
import com.example.crm_system.service.PackageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@CrossOrigin(origins = "*")

@RequestMapping("/api/customer-packages")
@RequiredArgsConstructor
@Validated
public class CustomerPackageController {

    private final CustomerPackageService customerPackageService;

    /**
     * Assign a package to a customer
     * POST /api/customer-packages/assign
     */
    @PostMapping("/assign")
    public ResponseEntity<CustomerPackageResponse> assignPackage(
            @Valid @RequestBody AssignPackageRequest request) {
        CustomerPackageResponse response = customerPackageService.assignPackageToCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get customer package dashboard with full details
     * GET /api/customer-packages/{id}/dashboard
     */
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<CustomerPackageDashboard> getPackageDashboard(@PathVariable Long id) {
        CustomerPackageDashboard dashboard = customerPackageService.getCustomerPackageDashboard(id);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get all packages for a customer
     * GET /api/customer-packages?customerId=1&status=ACTIVE
     */
    @GetMapping
    public ResponseEntity<List<CustomerPackageResponse>> getCustomerPackages(
            @RequestParam Long customerId,
            @RequestParam(required = false) CustomerPackage.PackageStatus status) {
        List<CustomerPackageResponse> response = 
            customerPackageService.getCustomerPackages(customerId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active packages for a customer
     * GET /api/customer-packages/active?customerId=1
     */
    @GetMapping("/active")
    public ResponseEntity<List<CustomerPackageResponse>> getActivePackages(
            @RequestParam Long customerId) {
        List<CustomerPackageResponse> response = 
            customerPackageService.getActiveCustomerPackages(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update package status
     * PATCH /api/customer-packages/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<CustomerPackageResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam CustomerPackage.PackageStatus status) {
        CustomerPackageResponse response = customerPackageService.updatePackageStatus(id, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Extend package validity
     * PATCH /api/customer-packages/{id}/extend
     */
    @PatchMapping("/{id}/extend")
    public ResponseEntity<CustomerPackageResponse> extendValidity(
            @PathVariable Long id,
            @RequestParam Integer additionalDays) {
        CustomerPackageResponse response = 
            customerPackageService.extendPackageValidity(id, additionalDays);
        return ResponseEntity.ok(response);
    }
}
