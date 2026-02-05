package com.example.crm_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder

@AllArgsConstructor
@NoArgsConstructor
public class PackageResponse {
    private Long id;
    private Long businessId;
    private String packageName;
    private String description;
    private Integer totalSessions;
    private Integer validityInDays;
    private BigDecimal originalPrice;
    private PackageEntity.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal finalPrice;
    private BigDecimal gst;
    private Boolean active;
    private List<ServiceMappingResponse> services;
    private List<ProductMappingResponse> products;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
