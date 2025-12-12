
package com.example.crm_system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "services")
@Data
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many services belong to one business
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore

    private Business business;

    @Column(nullable = false)
    private String serviceName;

    private String description;

    private int duration; 

    private BigDecimal price;

    private String discountPackage;

    private String genderPreference;

    // Multiple staff assignments (stored as string list)
    @ElementCollection
    @CollectionTable(name = "service_staff", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "staff_name")
    private List<String> staffAssignments;
}
