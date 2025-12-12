package com.example.crm_system.service;

import com.example.crm_system.entity.Business;
import com.example.crm_system.entity.ServiceEntity;
import com.example.crm_system.repository.BusinessRepository;
import com.example.crm_system.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;

    public ServiceService(ServiceRepository serviceRepository, BusinessRepository businessRepository) {
        this.serviceRepository = serviceRepository;
        this.businessRepository = businessRepository;
    }

    public ServiceEntity addService(Long businessId, ServiceEntity service) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found with id " + businessId));

        service.setBusiness(business);
        return serviceRepository.save(service);
    }

    public List<ServiceEntity> getServicesByBusiness(Long businessId) {
        return serviceRepository.findByBusinessId(businessId);
    }

    public Optional<ServiceEntity> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    public ServiceEntity updateService(Long id, ServiceEntity updatedService) {
        return serviceRepository.findById(id).map(service -> {
            service.setServiceName(updatedService.getServiceName());
            service.setDescription(updatedService.getDescription());
            service.setDuration(updatedService.getDuration());
            service.setPrice(updatedService.getPrice());
            service.setDiscountPackage(updatedService.getDiscountPackage());
            service.setGenderPreference(updatedService.getGenderPreference());
            service.setStaffAssignments(updatedService.getStaffAssignments());
            return serviceRepository.save(service);
        }).orElseThrow(() -> new RuntimeException("Service not found with id " + id));
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }
}
