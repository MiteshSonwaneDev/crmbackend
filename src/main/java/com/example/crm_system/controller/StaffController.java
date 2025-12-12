package com.example.crm_system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.crm_system.entity.StaffEntity;
import com.example.crm_system.service.StaffService;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")

public class StaffController {

    @Autowired
    private StaffService staffService;

    // Add staff to a business
    
    @PostMapping("/business/{businessId}")
    public ResponseEntity<StaffEntity> addStaff(@PathVariable Long businessId, @RequestBody StaffEntity staff) {
        return ResponseEntity.ok(staffService.addStaff(businessId, staff));
    }

    // Get all staff for a business
    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<StaffEntity>> getStaffByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(staffService.getStaffByBusiness(businessId));
    }

    // Get staff by ID
    @GetMapping("/{id}")
    public ResponseEntity<StaffEntity> getStaffById(@PathVariable Long id) {
        return staffService.getStaffById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update staff
    @PutMapping("/{id}")
    public ResponseEntity<StaffEntity> updateStaff(@PathVariable Long id, @RequestBody StaffEntity staffDetails) {
        return ResponseEntity.ok(staffService.updateStaff(id, staffDetails));
    }

    // Delete staff
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.ok("Staff deleted successfully");
    }
}
