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
public interface PackagePaymentRepository extends JpaRepository<PackagePayment, Long> {
    
    List<PackagePayment> findByCustomerPackageIdOrderByPaymentDateDesc(Long customerPackageId);
    
    List<PackagePayment> findByCustomerPackageCustomerIdOrderByPaymentDateDesc(Long customerId);
    
    @Query("SELECT pp FROM PackagePayment pp WHERE pp.customerPackage.packageEntity.business.id = :businessId " +
           "AND pp.paymentDate BETWEEN :startDate AND :endDate")
    List<PackagePayment> findPaymentsByBusinessAndDateRange(@Param("businessId") Long businessId,
                                                            @Param("startDate") java.time.LocalDateTime startDate,
                                                            @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT SUM(pp.amount) FROM PackagePayment pp WHERE pp.customerPackage.id = :packageId")
    java.math.BigDecimal getTotalPaymentsForPackage(@Param("packageId") Long packageId);
}
