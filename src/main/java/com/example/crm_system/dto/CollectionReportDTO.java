package com.example.crm_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionReportDTO {
    private double totalCash;
    private double totalCard;
    private double totalOthers;
    private double totalUserBalance;
}
