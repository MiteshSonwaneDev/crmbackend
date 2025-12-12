package com.example.crm_system.service;

import com.example.crm_system.entity.StaffEntity;
import com.example.crm_system.entity.Business;
import com.example.crm_system.repository.StaffRepository;
import com.example.crm_system.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private BusinessRepository businessRepository;

    public StaffEntity addStaff(Long businessId, StaffEntity staff) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found with id: " + businessId));
        staff.setBusiness(business);
        return staffRepository.save(staff);
    }

    // Get all staff for a business
    public List<StaffEntity> getStaffByBusiness(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found with id: " + businessId));
        return staffRepository.findByBusiness(business);
    }

    // Get single staff
    public Optional<StaffEntity> getStaffById(Long staffId) {
        return staffRepository.findById(staffId);
    }

    // Update staff (✅ now includes workCalendar)
    public StaffEntity updateStaff(Long staffId, StaffEntity staffDetails) {
        StaffEntity staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + staffId));

        staff.setName(staffDetails.getName());
        staff.setPhoneNumber(staffDetails.getPhoneNumber());
        staff.setDesignation(staffDetails.getDesignation());
        staff.setGender(staffDetails.getGender());
        staff.setGenderPreference(staffDetails.getGenderPreference());
        staff.setServicePreferences(staffDetails.getServicePreferences());
        
        staff.setWorkCalendar(staffDetails.getWorkCalendar());

        return staffRepository.save(staff);
    }

    public void deleteStaff(Long staffId) {
        staffRepository.deleteById(staffId);
    }
}
