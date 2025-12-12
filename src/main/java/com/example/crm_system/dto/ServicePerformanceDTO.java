package com.example.crm_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServicePerformanceDTO {
    private String serviceName;   // e.g., "Facial", "Hair Cut"
    private double revenue;       // Total revenue from this service
    private double percentage;    // Contribution % to total revenue
}
