package com.example.crm_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;


@Entity
@Table(name = "bill_settlements")
@Data

public class BillSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long businessId;
    private Long billId;
    private String billNumber;
    private LocalDateTime billDate;

    private String customerName;
    private String phoneNumber;
    private String customerGender;

    private Double serviceTotal;
    private Double productTotal;
    private Double discount;
    private Double gst;
    private Double netPayable;

    private Double cashAmount;
    private Double cardAmount;
    private Double otherAmount;
    private Double totalPaid;
    private Double amountToBeCollected;
    private Double userBalanceAmount;

    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    private LocalDateTime paymentDate;
}
