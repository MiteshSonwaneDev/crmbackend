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
public interface SessionProductConsumptionRepository extends JpaRepository<SessionProductConsumption, Long> {
    
    @Query("SELECT spc FROM SessionProductConsumption spc WHERE spc.packageSession.customerPackage.id = :packageId")
    List<SessionProductConsumption> findByCustomerPackageId(@Param("packageId") Long packageId);
    
    @Query("SELECT SUM(spc.quantityConsumed) FROM SessionProductConsumption spc " +
           "WHERE spc.product.id = :productId AND spc.packageSession.customerPackage.id = :packageId")
    java.math.BigDecimal getTotalConsumptionForProduct(@Param("packageId") Long packageId, 
                                                       @Param("productId") Long productId);
}
