package com.example.crm_system.service;

import com.example.crm_system.dto.*;
import com.example.crm_system.entity.*;
import com.example.crm_system.repository.BusinessRepository;
import com.example.crm_system.repository.CustomerRepository;
import com.example.crm_system.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
        private final WhatsAppService whatsAppService; // ✅ ADD THIS


    private LocalDateTime nowIST() {
        return ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
    }

    /**
     * Create a new lead and ensure corresponding customer exists
     */
    @Transactional
    public Lead createLead(Long businessId, LeadRequest request) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException(
                        "Business not found with ID: " + businessId
                ));

        Lead lead = Lead.builder()
                .createdAt(nowIST()) // ✅ IST time
                .source(request.getSource())
                .name(request.getName())
                .phone(request.getPhone())
                .query(request.getQuery())
                .status(LeadStatus.PENDING)
                .activities("NA")
                .business(business)
                .build();

        Lead savedLead = leadRepository.save(lead);
     try {

    // ✅ Send message to customer
    whatsAppService.sendCustomerWelcome(savedLead.getPhone(),business.getBusinessName(),savedLead.getName(),business.getReceptionNumber());

    // ✅ Send message to doctor

    whatsAppService.sendDoctorNotification(
             business.getBusinessName(),

            savedLead.getName(),
            savedLead.getPhone(),
            savedLead.getQuery(),
            savedLead.getSource()
    );

} catch (Exception e) {
    System.out.println("WhatsApp sending failed: " + e.getMessage());
}

        // Ensure customer exists for this business
        customerRepository.findByBusiness_IdAndCustomerNameAndCustomerMobileNumber(
                businessId, savedLead.getName(), savedLead.getPhone()
        ).orElseGet(() -> {
            CustomerEntity newCustomer = new CustomerEntity();
            newCustomer.setCustomerName(savedLead.getName());
            newCustomer.setCustomerMobileNumber(savedLead.getPhone());
            newCustomer.setBusiness(business);
            return customerRepository.save(newCustomer);
        });

        return savedLead;
    }
public void deleteLeadById(Long businessId, Long leadId) {
    Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new RuntimeException("Lead not found with ID: " + leadId));

    if (!lead.getBusiness().getId().equals(businessId)) {
        throw new RuntimeException("Lead does not belong to the specified business.");
    }

    leadRepository.delete(lead);
}

    /**
     * Mark lead as FOLLOWUP with follow-up datetime & comments
     */
    @Transactional
    public Lead markFollowup(Long leadId, Long businessId, FollowupRequest request) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found with ID: " + leadId));

        lead.setStatus(LeadStatus.FOLLOWUP);

        // Use provided follow-up date, or set default IST now
        lead.setFollowupDateTime(
                request.getFollowupDateTime() != null
                        ? request.getFollowupDateTime()
                        : nowIST()
        );
        lead.setFollowupComments(request.getFollowupComments());
        lead.setHotLead(request.getHotLead());

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + businessId));
        lead.setBusiness(business);

        return leadRepository.save(lead);
    }

    /**
     * Mark lead as CONVERTED and ensure customer exists
     */
    @Transactional
    public Lead markConverted(Long leadId, Long businessId, ClosingRequest request) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found with ID: " + leadId));

        lead.setStatus(LeadStatus.CONVERTED);
        lead.setClosingComments(request.getClosingComments());

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + businessId));
        lead.setBusiness(business);

        // Ensure customer exists for this business
        customerRepository.findByBusiness_IdAndCustomerNameAndCustomerMobileNumber(
                business.getId(), lead.getName(), lead.getPhone()
        ).orElseGet(() -> {
            CustomerEntity newCustomer = new CustomerEntity();
            newCustomer.setCustomerName(lead.getName());
            newCustomer.setCustomerMobileNumber(lead.getPhone());
            newCustomer.setBusiness(business);
            return customerRepository.save(newCustomer);
        });

        return leadRepository.save(lead);
    }

    /**
     * Mark lead as NOT INTERESTED
     */
    @Transactional
    public Lead markNotInterested(Long leadId, Long businessId, ClosingRequest request) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found with ID: " + leadId));

        lead.setStatus(LeadStatus.NOT_INTERESTED);
        lead.setClosingComments(request.getClosingComments());

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + businessId));
        lead.setBusiness(business);

        return leadRepository.save(lead);
    }

    /**
     * Get leads by status for a business
     */
    public List<Lead> getLeadsByStatus(Long businessId, LeadStatus status) {
        return leadRepository.findByBusiness_IdAndStatus(businessId, status);
    }

    /**
     * Get all leads for a business
     */
    public List<Lead> getAllLeadsByBusiness(Long businessId) {
        return leadRepository.findByBusiness_Id(businessId);
    }

    public List<Lead> getLeadsByPhone(Long businessId, String phone) {
    return leadRepository.findByPhoneAndBusiness_Id(phone, businessId);
}

}
