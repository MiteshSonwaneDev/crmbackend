package com.example.crm_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "whatsapp_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String doctorWhatsAppNumber;

    @Column(nullable = false)
    private Boolean enableAppointmentNotifications = true;

    @Column(nullable = false)
    private Boolean enableBillNotifications = true;

    @Column(nullable = false)
    private Boolean enableLeadNotifications = true;

    @Column(nullable = false)
    private Long businessId; // To support multi-business setup in future
}