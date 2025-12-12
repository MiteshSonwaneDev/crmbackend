package com.example.crm_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBalanceReportDTO {
    private String customerName;
    private String phoneNumber;
    private double balanceAmount;
}
