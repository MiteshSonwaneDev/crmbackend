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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface PackageSessionRepository extends JpaRepository<PackageSession, Long> {
    
    List<PackageSession> findByCustomerPackageIdOrderBySessionDateDesc(Long customerPackageId);
    
    List<PackageSession> findByCustomerPackageCustomerIdOrderBySessionDateDesc(Long customerId);
    
    @Query("SELECT ps FROM PackageSession ps WHERE ps.customerPackage.id = :packageId " +
           "ORDER BY ps.sessionNumber DESC")
    List<PackageSession> findSessionsByPackageId(@Param("packageId") Long packageId);
    
    @Query("SELECT MAX(ps.sessionNumber) FROM PackageSession ps WHERE ps.customerPackage.id = :packageId")
    Integer findMaxSessionNumber(@Param("packageId") Long packageId);
    
    @Query("SELECT ps FROM PackageSession ps WHERE ps.customerPackage.packageEntity.business.id = :businessId " +
           "AND ps.sessionDate BETWEEN :startDate AND :endDate")
    List<PackageSession> findSessionsByBusinessAndDateRange(@Param("businessId") Long businessId,
                                                            @Param("startDate") java.time.LocalDateTime startDate,
                                                            @Param("endDate") java.time.LocalDateTime endDate);
}
