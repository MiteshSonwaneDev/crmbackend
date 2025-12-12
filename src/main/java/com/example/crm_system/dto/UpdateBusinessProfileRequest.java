package com.example.crm_system.dto;

import lombok.Data;

@Data
public class UpdateBusinessProfileRequest {

    private String businessName;
    private String city;
    private String locality;
    private String address;
    private String ownerMobile;
    private String email;
    private String receptionNumber;
    private String appointmentMobile;
    private String workingDays;
    private String openingTime;
    private String closingTime;

}
