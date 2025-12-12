package com.example.crm_system.repository;

import com.example.crm_system.entity.StaffEntity;
import com.example.crm_system.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<StaffEntity, Long> {
    List<StaffEntity> findByBusiness(Business business);
}
