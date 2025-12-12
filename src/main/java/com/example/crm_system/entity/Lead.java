package com.example.crm_system.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;
    private String source;
    private String name;
    private String phone;
    private String query;

    @Enumerated(EnumType.STRING)
    private LeadStatus status;

    private String activities;

    private LocalDateTime followupDateTime;
    private String followupComments;
    private Boolean hotLead;

    private String closingComments;

   @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id") // this creates the foreign key in DB
    private Business business;
}
