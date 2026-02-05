package com.example.crm_system.entity;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.web.bind.annotation.SessionAttributes;
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "package_sessions")
public class PackageSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_package_id", nullable = false)
    private CustomerPackage customerPackage;

    @NotNull(message = "Session date is required")
    @Column(nullable = false)
    private LocalDateTime sessionDate;

    @Column(nullable = false)
    private Integer sessionNumber; // 1, 2, 3... tracking

    @Column(length = 100)
    private String performedByStaff; // Staff ID or name who performed session

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String customerFeedback;

    // Services performed in this session
    @OneToMany(mappedBy = "packageSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SessionServiceRecord> servicesPerformed = new ArrayList<>();

    // Products consumed in this session
    @OneToMany(mappedBy = "packageSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SessionProductConsumption> productsConsumed = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void addServiceRecord(SessionServiceRecord record) {
        servicesPerformed.add(record);
        record.setPackageSession(this);
    }

    public void addProductConsumption(SessionProductConsumption consumption) {
        productsConsumed.add(consumption);
        consumption.setPackageSession(this);
    }
}