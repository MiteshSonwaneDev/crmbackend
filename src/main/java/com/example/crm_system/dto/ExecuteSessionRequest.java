package com.example.crm_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.crm_system.entity.CustomerPackage;
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
public class ExecuteSessionRequest {
    
    @NotNull(message = "Customer package ID is required")
    private Long customerPackageId;
    
    @NotNull(message = "Session date is required")
    private LocalDateTime sessionDate;
    
    private String performedByStaff;
    
    private String notes;
    
    @Valid
    private List<SessionServiceRequest> services;
    
    @Valid
    private List<SessionProductRequest> products;
}
