package com.example.crm_system.dto;

import lombok.Data;

@Data
public class ServiceDto {
    private Long id;
    private String name;
    private Double price;
    private Integer quantity;
    private String duration;
}
