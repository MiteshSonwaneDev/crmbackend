package com.example.crm_system.dto;

import lombok.Data;

@Data
public class BillItemDTO {
    private String name;
    private String type; // SERVICE or PRODUCT
    private int quantity;
    private double rate;
    private String description; // only for products
    private String duration;   // only for services
}
