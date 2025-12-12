package com.example.crm_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceReportDTO {
    private String category;   // SERVICE or PRODUCT
    private String name;       // e.g. "Massage", "Shampoo"
    private long count;        // number of times sold
    private double totalCost;  // total revenue
    private double revenuePercent;
}
