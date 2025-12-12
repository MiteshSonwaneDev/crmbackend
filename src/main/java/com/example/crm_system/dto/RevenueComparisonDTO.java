package com.example.crm_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueComparisonDTO {
    private String month;     
    private double revenue;     
    private double avg3Months;  // last 3 months revenue
    
}
