package com.example.crm_system.service;

import com.example.crm_system.dto.UpdateBusinessProfileRequest;
import com.example.crm_system.entity.Business;
import com.example.crm_system.repository.BusinessRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    // ===================== CREATE / UPDATE BUSINESS =====================
    public Business saveBusiness(Business business) {
        return businessRepository.save(business);
    }

    // ===================== UPDATE LOGO =====================
    public boolean updateLogo(Long id, String base64Image) {
        return businessRepository.findById(id).map(business -> {
            business.setBusinessLogo(base64Image);
            businessRepository.save(business);
            return true;
        }).orElse(false);
    }

    // ===================== UPDATE SIGNATURE =====================
    public boolean updateSignature(Long id, String base64Image) {
        return businessRepository.findById(id).map(business -> {
            business.setSignatureImage(base64Image);
            businessRepository.save(business);
            return true;
        }).orElse(false);
    }

    // ===================== GET ALL BUSINESSES =====================
    public List<Business> getAllBusinesses() {
        return businessRepository.findAll();
    }

    // ===================== GET BUSINESS BY ID =====================
    public Optional<Business> getBusinessById(Long id) {
        return businessRepository.findById(id);
    }

    // ===================== LOGIN =====================
    public Optional<Business> login(String identifier, String password) {

        if (identifier.contains("@")) {
            return businessRepository.findByEmailAndPassword(identifier, password);
        } else {
            return businessRepository.findByOwnerMobileAndPassword(identifier, password);
        }
    }

    // ===================== UPDATE PASSWORD =====================
    public String updatePassword(String identifier, String currentPassword, String newPassword) {

        if (identifier == null || identifier.isBlank()) return "Identifier is required";
        if (currentPassword == null || currentPassword.isBlank()) return "Current password is required";
        if (newPassword == null || newPassword.isBlank()) return "New password is required";

        Optional<Business> businessOpt;

        if (identifier.contains("@")) {
            businessOpt = businessRepository.findByEmailAndPassword(identifier, currentPassword);
        } else {
            businessOpt = businessRepository.findByOwnerMobileAndPassword(identifier, currentPassword);
        }

        if (businessOpt.isEmpty()) {
            return "Invalid identifier or current password";
        }

        Business business = businessOpt.get();
        business.setPassword(newPassword);
        businessRepository.save(business);

        return "Password updated successfully";
    }

    // ==================================================================
    // 🔥🔥 EXTRA METHODS FOR IMAGES (USED IN YOUR CONTROLLER)
    // ==================================================================

    // ===================== GET BOTH IMAGES =====================
    public Optional<Object> getImages(Long id) {
        return businessRepository.findById(id).map(business -> {
            return new Object() {
                public final String logo = business.getBusinessLogo();
                public final String signature = business.getSignatureImage();
            };
        });
    }

    // ===================== GET ONLY LOGO =====================
    public Optional<String> getLogo(Long id) {
        return businessRepository.findById(id).map(Business::getBusinessLogo);
    }

    // ===================== GET ONLY SIGNATURE =====================
    public Optional<String> getSignature(Long id) {
        return businessRepository.findById(id).map(Business::getSignatureImage);
    }

    // ===================== DELETE LOGO =====================
    public boolean deleteLogo(Long id) {
        return businessRepository.findById(id).map(business -> {
            business.setBusinessLogo(null);
            businessRepository.save(business);
            return true;
        }).orElse(false);
    }

    // ===================== DELETE SIGNATURE =====================
    public boolean deleteSignature(Long id) {
        return businessRepository.findById(id).map(business -> {
            business.setSignatureImage(null);
            businessRepository.save(business);
            return true;
        }).orElse(false);
    }

    public Business updateProfile(Long id, UpdateBusinessProfileRequest request) {

    return businessRepository.findById(id).map(business -> {

        if (request.getBusinessName() != null)
            business.setBusinessName(request.getBusinessName());

        if (request.getCity() != null)
            business.setCity(request.getCity());

        if (request.getLocality() != null)
            business.setLocality(request.getLocality());

        if (request.getAddress() != null)
            business.setAddress(request.getAddress());

        if (request.getOwnerMobile() != null)
            business.setOwnerMobile(request.getOwnerMobile());

        if (request.getEmail() != null)
            business.setEmail(request.getEmail());

        if (request.getReceptionNumber() != null)
            business.setReceptionNumber(request.getReceptionNumber());

        if (request.getAppointmentMobile() != null)
            business.setAppointmentMobile(request.getAppointmentMobile());

        if (request.getWorkingDays() != null)
            business.setWorkingDays(request.getWorkingDays());

        if (request.getOpeningTime() != null)
            business.setOpeningTime(request.getOpeningTime());

        if (request.getClosingTime() != null)
            business.setClosingTime(request.getClosingTime());

        // IMPORTANT: Do NOT change password, logo, signature, verified flag
        return businessRepository.save(business);

    }).orElse(null);
}


}
