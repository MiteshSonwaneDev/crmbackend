package com.example.crm_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

@Entity
@Table(name = "business")
@Data
public class Business {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
@Column(nullable = false)
private String businessName;
private String city;
private String password;
private String locality;
private String address;
@Column(unique = true, nullable = false)
private String ownerMobile;
@Column(unique = true)
private String email;
private String receptionNumber;
private String appointmentMobile;
private String workingDays;
private String openingTime;
private String closingTime;
private boolean verified = false;
@Column(columnDefinition = "TEXT")
private String businessLogo;
@Column(columnDefinition = "TEXT")
private String signatureImage;

}
