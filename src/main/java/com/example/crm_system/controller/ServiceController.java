package com.example.crm_system.controller;

import com.example.crm_system.entity.ServiceEntity;
import com.example.crm_system.service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    // Add a service under a business
    @PostMapping("/business/{businessId}")
    public ResponseEntity<ServiceEntity> addService(@PathVariable Long businessId, @RequestBody ServiceEntity service) {
        return ResponseEntity.ok(serviceService.addService(businessId, service));
    }

    // Get all services for a business
    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<ServiceEntity>> getServicesByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(serviceService.getServicesByBusiness(businessId));
    }

    // Get single service by id
    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntity> getServiceById(@PathVariable Long id) {
        return serviceService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update a service
    @PutMapping("/{id}")
    public ResponseEntity<ServiceEntity> updateService(@PathVariable Long id, @RequestBody ServiceEntity updatedService) {
        return ResponseEntity.ok(serviceService.updateService(id, updatedService));
    }

    // Delete a service
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
