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
@Data
@Builder
@NoArgsConstructor      // ✅ ADD
@AllArgsConstructor
@Entity
@Table(name = "customer_packages")

public class CustomerPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private PackageEntity packageEntity;

    @NotNull(message = "Start date is required")
    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @NotNull(message = "Total sessions is required")
    @Column(nullable = false)
    private Integer totalSessions;

    @NotNull(message = "Remaining sessions is required")
    @Column(nullable = false)
    private Integer remainingSessions;

    @NotNull(message = "Total amount is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dueAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PackageStatus status = PackageStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Relationships
    @OneToMany(mappedBy = "customerPackage", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PackageSession> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "customerPackage", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PackagePayment> payments = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Automatically calculate expiry date and due amount before persisting
     */
    @PrePersist
    public void prePersist() {
        if (startDate != null && expiryDate == null && packageEntity != null) {
            this.expiryDate = startDate.plusDays(packageEntity.getValidityInDays());
        }
        calculateDueAmount();
    }

    @PreUpdate
    public void preUpdate() {
        calculateDueAmount();
        updateStatusBasedOnConditions();
    }

    /**
     * Calculate due amount based on total and paid amounts
     */
    public void calculateDueAmount() {
        if (totalAmount != null && paidAmount != null) {
            this.dueAmount = totalAmount.subtract(paidAmount);
            // Ensure due amount doesn't go negative
            if (this.dueAmount.compareTo(BigDecimal.ZERO) < 0) {
                this.dueAmount = BigDecimal.ZERO;
            }
        }
    }

    /**
     * Update status based on business rules
     */
    private void updateStatusBasedOnConditions() {
        if (remainingSessions != null && remainingSessions == 0 && status == PackageStatus.ACTIVE) {
            this.status = PackageStatus.COMPLETED;
        }
        if (expiryDate != null && LocalDate.now().isAfter(expiryDate) && status == PackageStatus.ACTIVE) {
            if (remainingSessions > 0) {
                this.status = PackageStatus.EXPIRED;
            }
        }
    }

    /**
     * Add a session and update remaining sessions
     */
    public void addSession(PackageSession session) {
        sessions.add(session);
        session.setCustomerPackage(this);
    }

    /**
     * Add a payment and update paid amount
     */
    public void addPayment(PackagePayment payment) {
        payments.add(payment);
        payment.setCustomerPackage(this);
        this.paidAmount = this.paidAmount.add(payment.getAmount());
    }

    public enum PackageStatus {
        ACTIVE,      // Package is active and can be used
        PAUSED,      // Temporarily paused by staff/customer
        COMPLETED,   // All sessions completed
        CANCELLED,   // Package cancelled
        EXPIRED      // Validity period ended
    }
}