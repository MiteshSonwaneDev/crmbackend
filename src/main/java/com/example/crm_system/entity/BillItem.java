package com.example.crm_system.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bill_item")
@Data
public class BillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemType; // SERVICE / PRODUCT
    private String name;
    private double price;
    private int quantity;
    private String duration;
    private String description; 


    // Many items belong to one bill
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
@JsonBackReference
    private BillEntity bill;
    
}
