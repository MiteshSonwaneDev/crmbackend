package com.example.crm_system.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "services")
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

    @ElementCollection
    @CollectionTable(name = "service_staff", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "staff_name")
    private List<String> staffAssignments;

    // ===== GETTERS =====

    public Long getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

    public int getDuration() {
        return duration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDiscountPackage() {
        return discountPackage;
    }

    public String getGenderPreference() {
        return genderPreference;
    }

    public List<String> getStaffAssignments() {
        return staffAssignments;
    }

    // ===== SETTERS =====

    public void setId(Long id) {
        this.id = id;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setDiscountPackage(String discountPackage) {
        this.discountPackage = discountPackage;
    }

    public void setGenderPreference(String genderPreference) {
        this.genderPreference = genderPreference;
    }

    public void setStaffAssignments(List<String> staffAssignments) {
        this.staffAssignments = staffAssignments;
    }
}
