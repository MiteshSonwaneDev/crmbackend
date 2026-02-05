package com.example.crm_system.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerPaymentSummary {
    private String customerName;
    private String phoneNumber;
    private int totalTransactions;
    private double totalBusiness;
    private List<String> services;      // Keep for summary (unique service names)
    private List<String> products;      // Keep for summary (unique product names)
    private List<PaymentDetail> payments;

    @Data
    public static class PaymentDetail {
        private Long billId;
        private LocalDateTime billDate;
        private double totalPaid;
        private double amountToBeCollected;
        private double cashAmount;
        private double cardAmount;
        private double otherAmount;
        private double userBalanceAmount;
        
        // ✅ Changed from List<String> to List<ItemDetail>
        private List<ItemDetail> services;
        private List<ItemDetail> products;
    }
    
    // ✅ New inner class for item details with quantity
    @Data
    public static class ItemDetail {
        private String name;
        private Integer quantity;
        private Double price;
        private Double total;
    }
}