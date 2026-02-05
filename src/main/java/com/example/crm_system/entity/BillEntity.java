package com.example.crm_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "bill")
@Data
public class BillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String billNumber;

    @Column(nullable = false)
    private LocalDateTime billDate;

    @Column(nullable = false)
    private double serviceTotal;

    @Column(nullable = false)
    private double productTotal;

    @Column(nullable = true)
    private double discount;   
    @Column(nullable = true)
    private double gst;        // GST percentage

    @Column(nullable = false)
    private double netPayable;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String customerGender;

    private String staffName;

    // ✅ Bill Items (cannot be null, but can be empty list)
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<BillItem> items;

    // ✅ Every bill must belong to a business
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    // ✅ Payment will be optional (because bill can exist without immediate payment)
    @OneToOne(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BillPayment payment;
}
