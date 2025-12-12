package com.example.crm_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionComparisonDTO {
    private String month;     // e.g., "Apr, 25"
    private long transactions; // Total number of transactions in that month
    private double avg3Months; // Average transactions over last 3 months
}
