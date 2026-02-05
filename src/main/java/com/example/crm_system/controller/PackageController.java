package com.example.crm_system.controller;

import com.example.crm_system.dto.*;
import com.example.crm_system.entity.Lead;
import com.example.crm_system.entity.LeadStatus;
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

@RequestMapping("/api/packages")
@RequiredArgsConstructor
@Validated
public class PackageController {

    private final PackageService packageService;

    /**
     * Create a new package template
     * POST /api/packages
     */
    @PostMapping
    public ResponseEntity<PackageResponse> createPackage(@Valid @RequestBody CreatePackageRequest request) {
        PackageResponse response = packageService.createPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update existing package
     * PUT /api/packages/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PackageResponse> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody CreatePackageRequest request) {
        PackageResponse response = packageService.updatePackage(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get package by ID
     * GET /api/packages/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PackageResponse> getPackageById(@PathVariable Long id) {
        PackageResponse response = packageService.getPackageById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all packages for a business
     * GET /api/packages?businessId=1&activeOnly=true
     */
    @GetMapping
    public ResponseEntity<List<PackageResponse>> getAllPackages(
            @RequestParam Long businessId,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
        List<PackageResponse> response = packageService.getAllPackages(businessId, activeOnly);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete (deactivate) a package
     * DELETE /api/packages/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }
}
