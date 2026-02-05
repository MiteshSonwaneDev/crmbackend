package com.example.crm_system.controller;

import com.example.crm_system.dto.*;
import com.example.crm_system.entity.CustomerPackage;
import com.example.crm_system.entity.Lead;
import com.example.crm_system.entity.LeadStatus;
import com.example.crm_system.service.CustomerPackageService;
import com.example.crm_system.service.LeadService;
import com.example.crm_system.service.PackageService;
import com.example.crm_system.service.SessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")

@Validated
public class SessionController {

    private final SessionService sessionService;

    /**
     * Execute a session for a customer package
     * POST /api/sessions/execute
     */
    @PostMapping("/execute")
    public ResponseEntity<SessionResponse> executeSession(
            @Valid @RequestBody ExecuteSessionRequest request) {
        SessionResponse response = sessionService.executeSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get session history for a customer package
     * GET /api/sessions?customerPackageId=1
     */
    @GetMapping
    public ResponseEntity<List<SessionResponse>> getSessionHistory(
            @RequestParam(required = false) Long customerPackageId,
            @RequestParam(required = false) Long customerId) {
        
        List<SessionResponse> response;
        if (customerPackageId != null) {
            response = sessionService.getSessionHistory(customerPackageId);
        } else if (customerId != null) {
            response = sessionService.getCustomerSessionHistory(customerId);
        } else {
            throw new IllegalArgumentException("Either customerPackageId or customerId is required");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get session by ID
     * GET /api/sessions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSessionById(@PathVariable Long id) {
        SessionResponse response = sessionService.getSessionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update session notes
     * PATCH /api/sessions/{id}/notes
     */
    @PatchMapping("/{id}/notes")
    public ResponseEntity<SessionResponse> updateNotes(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String customerFeedback) {
        SessionResponse response = sessionService.updateSessionNotes(id, notes, customerFeedback);
        return ResponseEntity.ok(response);
    }
}
