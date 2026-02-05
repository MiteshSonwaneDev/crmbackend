package com.example.crm_system.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BillWithPaymentDTO {

    private Long id;
    private String billNumber;
    private LocalDateTime billDate;

    private double serviceTotal;
    private double productTotal;
    private double discount;
    private double gst;
    private double netPayable;

    private String customerName;
    private String phoneNumber;
    private String customerGender;
    private String staffName;

    private List<BillItemDTO> items;
    private BillPaymentResponseDTO payment;
}
