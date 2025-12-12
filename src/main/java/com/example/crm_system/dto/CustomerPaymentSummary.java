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
    private List<String> services;
    private List<String> products;
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
        private List<String> services;
        private double userBalanceAmount;
    }
}
