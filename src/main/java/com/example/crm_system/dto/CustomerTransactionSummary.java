package com.example.crm_system.dto;

import lombok.Data;
import java.util.List;

@Data
public class CustomerTransactionSummary {

    private String customerName;
    private String phoneNumber;

    private int totalTransactions;
    private double totalBusiness;
    private double averageAppointmentCost;

    private List<String> services;
    private List<String> products;

    private List<PaymentDetail> payments;

    @Data
    public static class PaymentDetail {
        private Long billId;
        private double totalPaid;
        private double amountToBeCollected;
        private double cashAmount;
        private double cardAmount;
        private double otherAmount;
        private double userBalanceAmount;
    }
}
