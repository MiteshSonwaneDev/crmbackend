package com.example.crm_system.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bill_payment")
@Data
public class BillPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long businessId;   // 👈 add this if you want direct mapping

   private Double cashAmount;
private Double cardAmount;
private Double otherAmount;
private Double userBalanceAmount;

    private String collectionNotes;
    private String customerNotes;
private LocalDateTime paymentDate; // ✅ payment date field

    // getters & setters
    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
   private Double totalPaid;
private Double amountToBeCollected;
@OneToOne
@JoinColumn(name = "bill_id")
@JsonIgnore
    private BillEntity bill;
    
}
