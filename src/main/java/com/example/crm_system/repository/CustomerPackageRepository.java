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
public interface CustomerPackageRepository extends JpaRepository<CustomerPackage, Long> {
    
    List<CustomerPackage> findByCustomerId(Long customerId);
    
    List<CustomerPackage> findByCustomerIdAndStatus(Long customerId, CustomerPackage.PackageStatus status);
    
    Optional<CustomerPackage> findByIdAndCustomerId(Long id, Long customerId);
    
    @Query("SELECT cp FROM CustomerPackage cp WHERE cp.customer.id = :customerId " +
           "AND cp.status = 'ACTIVE' AND cp.remainingSessions > 0 " +
           "AND cp.expiryDate >= :currentDate")
    List<CustomerPackage> findActivePackagesForCustomer(@Param("customerId") Long customerId,
                                                         @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT cp FROM CustomerPackage cp WHERE cp.packageEntity.business.id = :businessId " +
           "AND cp.status = :status")
    List<CustomerPackage> findByBusinessIdAndStatus(@Param("businessId") Long businessId,
                                                     @Param("status") CustomerPackage.PackageStatus status);
    
    @Query("SELECT cp FROM CustomerPackage cp WHERE cp.expiryDate < :currentDate " +
           "AND cp.status = 'ACTIVE'")
    List<CustomerPackage> findExpiredPackages(@Param("currentDate") LocalDate currentDate);
    
    // Dashboard statistics
    @Query("SELECT COUNT(cp) FROM CustomerPackage cp WHERE cp.packageEntity.business.id = :businessId " +
           "AND cp.status = 'ACTIVE'")
    Long countActivePackagesByBusiness(@Param("businessId") Long businessId);
    
    @Query("SELECT SUM(cp.dueAmount) FROM CustomerPackage cp WHERE cp.packageEntity.business.id = :businessId " +
           "AND cp.status IN ('ACTIVE', 'PAUSED')")
    java.math.BigDecimal getTotalPendingPayments(@Param("businessId") Long businessId);
}
