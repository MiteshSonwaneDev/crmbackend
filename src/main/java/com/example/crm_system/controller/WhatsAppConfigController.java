package com.example.crm_system.controller;

import com.example.crm_system.entity.WhatsAppConfig;
import com.example.crm_system.repository.WhatsAppConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WhatsAppConfigController {

    private final WhatsAppConfigRepository whatsAppConfigRepository;

    @GetMapping("/whatsapp-config")
    public ResponseEntity<WhatsAppConfig> getWhatsAppConfig() {
        log.info("📱 Fetching WhatsApp configuration");
        
        WhatsAppConfig config = whatsAppConfigRepository
                .findFirstByOrderByIdAsc()
                .orElse(null);
        
        if (config == null) {
            log.info("No configuration found, returning defaults");
            config = new WhatsAppConfig();
            config.setDoctorWhatsAppNumber("");
            config.setEnableAppointmentNotifications(true);
            config.setEnableBillNotifications(true);
            config.setEnableLeadNotifications(true);
        }
        
        return ResponseEntity.ok(config);
    }

    @PostMapping("/whatsapp-config")
    public ResponseEntity<WhatsAppConfig> saveWhatsAppConfig(
            @RequestBody WhatsAppConfig config) {
        
        log.info("💾 Saving WhatsApp configuration");
        log.info("Doctor Number: {}", config.getDoctorWhatsAppNumber());
        
        // For single business setup, always use businessId = 1
        config.setBusinessId(1L);
        
        // Check if configuration already exists
        WhatsAppConfig existingConfig = whatsAppConfigRepository
                .findFirstByOrderByIdAsc()
                .orElse(null);
        
        if (existingConfig != null) {
            // Update existing configuration
            existingConfig.setDoctorWhatsAppNumber(config.getDoctorWhatsAppNumber());
            existingConfig.setEnableAppointmentNotifications(config.getEnableAppointmentNotifications());
            existingConfig.setEnableBillNotifications(config.getEnableBillNotifications());
            existingConfig.setEnableLeadNotifications(config.getEnableLeadNotifications());
            config = existingConfig;
        }
        
        WhatsAppConfig savedConfig = whatsAppConfigRepository.save(config);
        
        log.info("✅ WhatsApp configuration saved successfully");
        return ResponseEntity.ok(savedConfig);
    }
}