package com.example.crm_system.service;

import com.example.crm_system.entity.Business;
import com.example.crm_system.entity.CustomerEntity;
import com.example.crm_system.repository.BusinessRepository;
import com.example.crm_system.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;

    public CustomerService(CustomerRepository customerRepository, BusinessRepository businessRepository) {
        this.customerRepository = customerRepository;
        this.businessRepository = businessRepository;
    }

    // Add a customer under a business
    public CustomerEntity addCustomer(Long businessId, CustomerEntity customer) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found with id: " + businessId));
        customer.setBusiness(business);
        return customerRepository.save(customer);
    }

    // Get all customers for a business
    public List<CustomerEntity> getCustomerByBusiness(Long businessId) {
        return customerRepository.findByBusiness_Id(businessId);
    }

    // Get a single customer by ID
    public Optional<CustomerEntity> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    // Update customer
    public CustomerEntity updateCustomer(Long id, CustomerEntity updatedCustomer) {
        return customerRepository.findById(id)
                .map(existing -> {
                    existing.setCustomerName(updatedCustomer.getCustomerName());
                    existing.setCustomerEmail(updatedCustomer.getCustomerEmail());
                    existing.setCustomerMobileNumber(updatedCustomer.getCustomerMobileNumber());
                    existing.setCustomerGender(updatedCustomer.getCustomerGender());
                    existing.setCustomerDOB(updatedCustomer.getCustomerDOB());
                    return customerRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    // Delete customer
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }
}
