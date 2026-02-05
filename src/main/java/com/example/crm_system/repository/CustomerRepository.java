package com.example.crm_system.repository;

import com.example.crm_system.dto.CustomerListDTO;
import com.example.crm_system.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    // Fetch all customers for a given business
List<CustomerListDTO> findByBusiness_Id(Long businessId);

    // Check if a customer already exists by name + mobile
 Optional<CustomerEntity> findByBusiness_IdAndCustomerNameAndCustomerMobileNumber(
        Long businessId, String customerName, String customerMobileNumber
    );

    Optional<CustomerEntity> findByBusinessIdAndCustomerMobileNumber(Long businessId, String customerMobileNumber);

}
