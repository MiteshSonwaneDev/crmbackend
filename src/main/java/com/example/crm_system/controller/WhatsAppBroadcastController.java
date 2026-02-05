package com.example.crm_system.controller;

import com.example.crm_system.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WhatsAppBroadcastController {

    private final WhatsAppService whatsAppService;

    @PostMapping("/send-broadcast")
    public ResponseEntity<Map<String, String>> sendBroadcastMessage(
            @RequestBody Map<String, String> request) {
        
        String phone = request.get("phone");
        String message = request.get("message");
        
        log.info("📢 Broadcasting message to: {}", phone);
        
        try {
            whatsAppService.sendMessage(phone, message);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Message sent successfully",
                "phone", phone
            ));
            
        } catch (Exception e) {
            log.error("❌ Broadcast failed for {}", phone, e);
            
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to send message",
                "phone", phone
            ));
        }
    }
}