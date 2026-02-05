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
public class ProductMappingRequest {
    
    @NotNull
    private Long productId;
    
    @NotNull @Min(1)
    private Integer quantityPerPackage;
    
    @NotNull @DecimalMin("0.01")
    private BigDecimal usagePerSession;
    
    private Boolean internalConsumptionOnly = false;
    private String usageInstructions;
}
