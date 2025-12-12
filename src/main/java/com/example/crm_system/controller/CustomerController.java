package com.example.crm_system.controller;

import com.example.crm_system.entity.CustomerEntity;
import com.example.crm_system.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Add a customer under a business
    @PostMapping("/business/{businessId}")
    public ResponseEntity<CustomerEntity> addCustomer(@PathVariable Long businessId, @RequestBody CustomerEntity customer) {
        return ResponseEntity.ok(customerService.addCustomer(businessId, customer));
    }

    // Get all customers for a business
    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<CustomerEntity>> getCustomerByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(customerService.getCustomerByBusiness(businessId));
    }


    
    
    // Get single customer by id
    @GetMapping("/{id}")
    public ResponseEntity<CustomerEntity> getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update a customer
    @PutMapping("/{id}")
    public ResponseEntity<CustomerEntity> updateCustomer(@PathVariable Long id, @RequestBody CustomerEntity updatedCustomer) {
        return ResponseEntity.ok(customerService.updateCustomer(id, updatedCustomer));
    }

    // Delete a customer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
