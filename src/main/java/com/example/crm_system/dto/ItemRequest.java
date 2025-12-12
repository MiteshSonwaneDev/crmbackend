package com.example.crm_system.dto;

import lombok.Data;

@Data
public class ItemRequest {
    private Long id;       
    private String name;   
    private double price;  
    private int quantity;  
    private String duration;
   private String description;


}
