package com.example.crm_system.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BillPaymentResponseDTO {
    private Long paymentId;
    private Long billId;
    private Long businessId;

    private String customerName;
    private String phoneNumber;
    private String customerGender;
    private LocalDateTime billDate;   // ✅ add this

    private Double cashAmount;
    private Double cardAmount;
    private Double otherAmount;
    private Double userBalanceAmount;
    private String collectionNotes;
    private String customerNotes;
    private Double totalPaid;
    private Double amountToBeCollected;
}
