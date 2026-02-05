package com.example.crm_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.crm_system.entity.CustomerPackage;
import com.example.crm_system.entity.Lead;
import com.example.crm_system.entity.LeadStatus;
import com.example.crm_system.entity.PackageEntity;
import com.example.crm_system.entity.PackagePayment;
import com.example.crm_system.entity.PackageSession;
import com.example.crm_system.entity.SessionProductConsumption;

import java.util.List;
import java.util.Optional;
@Repository
public interface PackageRepository extends JpaRepository<PackageEntity, Long> {
    
    List<PackageEntity> findByBusinessIdAndActiveTrue(Long businessId);
    
    List<PackageEntity> findByBusinessId(Long businessId);
    
    Optional<PackageEntity> findByIdAndBusinessId(Long id, Long businessId);
    
    @Query("SELECT p FROM PackageEntity p WHERE p.business.id = :businessId " +
           "AND p.active = true AND LOWER(p.packageName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<PackageEntity> searchActivePackages(@Param("businessId") Long businessId, 
                                             @Param("search") String search);
}


