package com.example.crm_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionReportDTO {
    private long transactions;
    private double totalRevenue;
    private double cash;
    private double card;
    private double others;
}
