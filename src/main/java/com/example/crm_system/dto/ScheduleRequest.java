package com.example.crm_system.dto;

import lombok.Data;
import java.util.List;

@Data
public class ScheduleRequest {
    private Long businessId;
    private String customerName;
        private String staffName;

    private String phoneNumber;
    private String customerGender;
    private List<ServiceDto> services;   
    private List<ProductDTO> products;   
    private String appointmentDateTime;
}
