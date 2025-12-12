package com.example.crm_system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BillDetailsResponseDTO {
    private Long billId;
    private String billNumber;
    private LocalDateTime billDate;
    private String customerName;
    private String phoneNumber;
    private String customerGender;

    private double serviceTotal;
    private double productTotal;
    private double discount;
    private double gst;
    private double netPayable;

    private List<BillItemDTO> items;
    private BillPaymentResponseDTO payment;
}
