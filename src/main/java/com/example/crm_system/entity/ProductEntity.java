package com.example.crm_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "product")
@Data
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many products belong to one business
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Business business;

    @Column(nullable = false)
    private String productName;

    private String brand;
    private String singleUnitSize;
    private String productDescription;
    private Integer quantity;
    private String code;
    private String type;
    private Integer GST;
    private BigDecimal   unitSellingPrice;

    @Column(nullable = true)
    private Integer minStockQuantity;

    @Column(nullable = true)
    private Boolean lowstockalert;  

    private Boolean internalConsumptionOnly;
    private String supplier;
    private String invoiceNumber;
    private Double totalBillAmount;
     @Column(name = "usage_quantity")
    private Integer usage = 0;   

    @UpdateTimestamp
    private LocalDateTime updated;  
}
