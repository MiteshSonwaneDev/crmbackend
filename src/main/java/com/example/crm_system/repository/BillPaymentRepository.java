package com.example.crm_system.repository;

import com.example.crm_system.entity.BillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {

    Optional<BillPayment> findByBillId(Long billId);

    // Original method
    @Query("SELECT p FROM BillPayment p WHERE p.bill.billDate BETWEEN :start AND :end")
    List<BillPayment> findPaymentsBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // ✅ New method for business-specific reports
    @Query("SELECT p FROM BillPayment p WHERE p.bill.billDate BETWEEN :start AND :end AND p.bill.business.id = :businessId")
    List<BillPayment> findPaymentsBetweenByBusiness(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("businessId") Long businessId
    );
        List<BillPayment> findByBill_Business_Id(Long businessId);


}
