package com.example.crm_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.crm_system.entity.Lead;
import com.example.crm_system.entity.LeadStatus;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByBusiness_IdAndStatus(Long businessId, LeadStatus status); // <-- works because of @ManyToOne
    List<Lead> findByBusiness_Id(Long businessId);
    List<Lead> findByPhoneAndBusiness_Id(String phone, Long businessId);
    void deleteByIdAndBusinessId(Long id, Long businessId);


}
