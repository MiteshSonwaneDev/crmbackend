package com.example.crm_system.dto;

import lombok.Data;

@Data   
public class ProductDTO {
    private Long id;
    private String name;
    private Double price;
    private Integer quantity;
    private String description;
}
