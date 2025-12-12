package com.example.crm_system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BillRequest {

    private Long businessId;

    private String customerName;
    private String phoneNumber;
    private String customerGender;
    private String staffName;
private LocalDateTime billDate;
    private List<ItemRequest> services;   // list of services
    private List<ItemRequest> products;   // list of products

    private double discount; // % discount
    private double gst;      // GST percentage
}
