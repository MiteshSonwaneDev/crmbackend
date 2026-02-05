

package com.example.crm_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.crm_system.entity.CustomerPackage;
import com.example.crm_system.entity.PackageEntity;
import com.example.crm_system.entity.PackagePayment;

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
public class AddPaymentRequest {
    
    @NotNull(message = "Customer package ID is required")
    private Long customerPackageId;
    
    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;
    
    @NotNull
    private LocalDateTime paymentDate;
    
    @NotNull
    private PackagePayment.PaymentMethod paymentMethod;
    
    private String transactionReference;
    private String invoiceNumber;
    private String notes;
    private String receivedBy;
}