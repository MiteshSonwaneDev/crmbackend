package com.example.crm_system.controller;

import com.example.crm_system.entity.Business;
import com.example.crm_system.dto.LoginRequest;
import com.example.crm_system.dto.UpdateBusinessProfileRequest;
import com.example.crm_system.dto.UpdatePasswordRequest;
import com.example.crm_system.service.BusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business")
@CrossOrigin(origins = "*")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    // ================== REGISTER ==================
    @PostMapping("/register")
    public ResponseEntity<Business> registerBusiness(@RequestBody Business business) {
        return ResponseEntity.ok(businessService.saveBusiness(business));
    }

    // ================== LOGIN ==================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return businessService.login(request.getIdentifier(), request.getPassword())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }

    // ================== UPLOAD LOGO ==================
    @PostMapping("/{id}/upload-logo")
    public ResponseEntity<?> uploadLogo(
            @PathVariable Long id,
            @RequestBody String base64Image) {

        return businessService.updateLogo(id, base64Image)
                ? ResponseEntity.ok("Logo uploaded successfully")
                : ResponseEntity.badRequest().body("Business not found");
    }

    // ================== UPLOAD SIGNATURE ==================
    @PostMapping("/{id}/upload-signature")
    public ResponseEntity<?> uploadSignature(
            @PathVariable Long id,
            @RequestBody String base64Image) {

        return businessService.updateSignature(id, base64Image)
                ? ResponseEntity.ok("Signature uploaded successfully")
                : ResponseEntity.badRequest().body("Business not found");
    }

    // ================== GET BOTH IMAGES ==================
    @GetMapping("/{id}/images")
    public ResponseEntity<?> getImages(@PathVariable Long id) {
        return businessService.getImages(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body("Business not found"));
    }

    // ================== GET LOGO ONLY ==================
    @GetMapping("/{id}/logo")
    public ResponseEntity<?> getLogo(@PathVariable Long id) {
        return businessService.getLogo(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body("Business not found"));
    }

    // ================== GET SIGNATURE ONLY ==================
    @GetMapping("/{id}/signature")
    public ResponseEntity<?> getSignature(@PathVariable Long id) {
        return businessService.getSignature(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body("Business not found"));
    }

    // ================== DELETE LOGO ==================
    @DeleteMapping("/{id}/logo")
    public ResponseEntity<?> deleteLogo(@PathVariable Long id) {
        return businessService.deleteLogo(id)
                ? ResponseEntity.ok("Logo deleted successfully")
                : ResponseEntity.badRequest().body("Business not found");
    }

    // ================== DELETE SIGNATURE ==================
    @DeleteMapping("/{id}/signature")
    public ResponseEntity<?> deleteSignature(@PathVariable Long id) {
        return businessService.deleteSignature(id)
                ? ResponseEntity.ok("Signature deleted successfully")
                : ResponseEntity.badRequest().body("Business not found");
    }

    // ================== GET ALL BUSINESSES ==================
    @GetMapping
    public ResponseEntity<List<Business>> getAllBusinesses() {
        return ResponseEntity.ok(businessService.getAllBusinesses());
    }

    // ================== GET BY ID ==================
    @GetMapping("/{id}")
    public ResponseEntity<Business> getBusinessById(@PathVariable Long id) {
        return businessService.getBusinessById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ================== UPDATE PASSWORD ==================
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest request) {
        String result = businessService.updatePassword(
                request.getIdentifier(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        if (result.equals("Password updated successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PutMapping("/{id}/update-profile")
public ResponseEntity<?> updateProfile(
        @PathVariable Long id,
        @RequestBody UpdateBusinessProfileRequest request) {

    Business updated = businessService.updateProfile(id, request);

    if (updated == null) {
        return ResponseEntity.badRequest().body("Business not found");
    }

    return ResponseEntity.ok(updated);
}

}
