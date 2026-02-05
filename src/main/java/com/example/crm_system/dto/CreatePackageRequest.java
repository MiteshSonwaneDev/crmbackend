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
public class CreatePackageRequest {
    
    @NotNull(message = "Business ID is required")
    private Long businessId;
    
    @NotBlank(message = "Package name is required")
    @Size(max = 200)
    private String packageName;
    
    private String description;
    
    @NotNull @Min(1)
    private Integer totalSessions;
    
    @NotNull @Min(1)
    private Integer validityInDays;
    
    @NotNull @DecimalMin("0.01")
    private BigDecimal originalPrice;
    
    private PackageEntity.DiscountType discountType;
    
    @DecimalMin("0.0")
    private BigDecimal discountValue;
    
    @DecimalMin("0.0") @DecimalMax("100.0")
    private BigDecimal gst;
    
    private Boolean active = true;
    
    @Valid
    private List<ServiceMappingRequest> services;
    
    @Valid
    private List<ProductMappingRequest> products;
}
