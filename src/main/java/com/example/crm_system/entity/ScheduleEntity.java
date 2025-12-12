package com.example.crm_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "schedule")
@Data
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String phoneNumber;
    private String customerGender;
    private String staffName;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("schedule-services")
    private List<ScheduleServiceEntity> services = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("schedule-products")
    private List<ScheduleProductEntity> products = new ArrayList<>();

    // 👇 Store as plain String (coming from frontend)
    private String appointmentDateTime;

    private String status; // PENDING, COMPLETED, CANCELLED

    // Link to Bill (optional, only when bill is generated later)
    @OneToOne
    @JoinColumn(name = "bill_id")
    private BillEntity bill;

    // Many schedules belong to one business
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}
