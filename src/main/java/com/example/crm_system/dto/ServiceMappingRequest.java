package com.example.crm_system.dto;

import java.math.BigDecimal;
import java.util.List;

import com.example.crm_system.entity.PackageEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceMappingRequest {
    
    @NotNull
    private Long serviceId;
    
    @NotNull @Min(1)
    private Integer sessionsForService;
    
    @NotNull @DecimalMin("0.0")
    private BigDecimal pricePerSession;
    
    private String staffPreference;
    private String notes;
}
