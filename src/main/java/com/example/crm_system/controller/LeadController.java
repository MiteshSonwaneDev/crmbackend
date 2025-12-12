package com.example.crm_system.controller;

import com.example.crm_system.dto.*;
import com.example.crm_system.entity.Lead;
import com.example.crm_system.entity.LeadStatus;
import com.example.crm_system.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    // Create a new lead for a specific business
    @PostMapping("/{businessId}")
    public Lead createLead(@PathVariable Long businessId, @RequestBody LeadRequest request) {
        return leadService.createLead(businessId, request);
    }

    // Mark a lead as follow-up for a specific business
    @PutMapping("/{businessId}/{id}/followup")
    public Lead followupLead(@PathVariable Long businessId, @PathVariable Long id,
                             @RequestBody FollowupRequest request) {
        // Swap: leadId first, then businessId
        return leadService.markFollowup(id, businessId, request);
    }

    // Mark a lead as converted for a specific business
    @PutMapping("/{businessId}/{id}/convert")
    public Lead convertLead(@PathVariable Long businessId, @PathVariable Long id,
                            @RequestBody ClosingRequest request) {
        // Swap: leadId first, then businessId
        return leadService.markConverted(id, businessId, request);
    }

    // Mark a lead as not interested for a specific business
    @PutMapping("/{businessId}/{id}/not-interested")
    public Lead notInterested(@PathVariable Long businessId, @PathVariable Long id,
                              @RequestBody ClosingRequest request) {
        // Swap: leadId first, then businessId
        return leadService.markNotInterested(id, businessId, request);
    }

    // Get leads by status for a specific business
    @GetMapping("/{businessId}/status/{status}")
    public List<Lead> getLeadsByStatus(@PathVariable Long businessId, @PathVariable LeadStatus status) {
        return leadService.getLeadsByStatus(businessId, status);
    }

    // Get follow-up leads for a specific business
    @GetMapping("/{businessId}/followup")
    public List<Lead> getFollowupLeads(@PathVariable Long businessId) {
        return leadService.getLeadsByStatus(businessId, LeadStatus.FOLLOWUP);
    }

    // Get converted leads for a specific business
    @GetMapping("/{businessId}/converted")
    public List<Lead> getConvertedLeads(@PathVariable Long businessId) {
        return leadService.getLeadsByStatus(businessId, LeadStatus.CONVERTED);
    }

    // Get not-interested leads for a specific business
    @GetMapping("/{businessId}/not-interested")
    public List<Lead> getNotInterestedLeads(@PathVariable Long businessId) {
        return leadService.getLeadsByStatus(businessId, LeadStatus.NOT_INTERESTED);
    }
    // Get leads of a customer by phone number for a specific business
@GetMapping("/{businessId}/customer/{phone}")
public List<Lead> getLeadsByCustomerPhone(@PathVariable Long businessId, @PathVariable String phone) {
    return leadService.getLeadsByPhone(businessId, phone);
}
// Delete a lead by ID for a specific business
@DeleteMapping("/{businessId}/{leadId}")
public String deleteLead(@PathVariable Long businessId, @PathVariable Long leadId) {
    leadService.deleteLeadById(businessId, leadId);
    return "Lead with ID " + leadId + " has been deleted successfully.";
}


}
