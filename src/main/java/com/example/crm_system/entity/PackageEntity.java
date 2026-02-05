package com.example.crm_system.entity;

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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Master template for treatment packages that can be assigned to customers.
 * Supports session-based treatments with services and product consumption.
 */
@Entity
@Table(name = "packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Business business;

    @NotBlank(message = "Package name is required")
    @Column(nullable = false, length = 200)
    private String packageName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Total sessions is required")
    @Min(value = 1, message = "Total sessions must be at least 1")
    @Column(nullable = false)
    private Integer totalSessions;

    @NotNull(message = "Validity in days is required")
    @Min(value = 1, message = "Validity must be at least 1 day")
    @Column(nullable = false)
    private Integer validityInDays;

    @NotNull(message = "Original price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DiscountType discountType;

    @DecimalMin(value = "0.0", message = "Discount value cannot be negative")
    @Column(precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @DecimalMin(value = "0.0", message = "GST cannot be negative")
    @DecimalMax(value = "100.0", message = "GST cannot exceed 100%")
    @Column(precision = 5, scale = 2)
    private BigDecimal gst;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "packageEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PackageServiceMapping> servicesMappings = new ArrayList<>();

    @OneToMany(mappedBy = "packageEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PackageProductMapping> productsMappings = new ArrayList<>();

    @OneToMany(mappedBy = "packageEntity", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CustomerPackage> customerPackages = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public void addServiceMapping(PackageServiceMapping mapping) {
        servicesMappings.add(mapping);
        mapping.setPackageEntity(this);
    }

    public void removeServiceMapping(PackageServiceMapping mapping) {
        servicesMappings.remove(mapping);
        mapping.setPackageEntity(null);
    }

    public void addProductMapping(PackageProductMapping mapping) {
        productsMappings.add(mapping);
        mapping.setPackageEntity(this);
    }

    public void removeProductMapping(PackageProductMapping mapping) {
        productsMappings.remove(mapping);
        mapping.setPackageEntity(null);
    }

    @PrePersist
    @PreUpdate
    public void calculateFinalPrice() {
        if (originalPrice == null) return;

        BigDecimal discountedPrice = originalPrice;

        if (discountType != null && discountValue != null && discountValue.compareTo(BigDecimal.ZERO) > 0) {
            if (discountType == DiscountType.FLAT) {
                discountedPrice = originalPrice.subtract(discountValue);
            } else if (discountType == DiscountType.PERCENTAGE) {
                BigDecimal discountAmount = originalPrice
                        .multiply(discountValue)
                        .divide(new BigDecimal("100"));
                discountedPrice = originalPrice.subtract(discountAmount);
            }
        }

        if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) {
            discountedPrice = BigDecimal.ZERO;
        }

        this.finalPrice = discountedPrice;
    }

    public enum DiscountType {
        FLAT,
        PERCENTAGE
    }
}
