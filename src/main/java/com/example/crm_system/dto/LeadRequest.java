package com.example.crm_system.dto;
import lombok.Data;

@Data
public class LeadRequest {
    private String source;
    private String name;
    private String phone;
    private String query;
      private Long businessId;
}
