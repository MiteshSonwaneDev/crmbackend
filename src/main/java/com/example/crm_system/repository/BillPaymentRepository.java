package com.example.crm_system.repository;

import com.example.crm_system.entity.BillEntity;
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

    // ✅ Find payment by bill id (used while updating / checking payment)
    Optional<BillPayment> findByBillId(Long billId);

    // ✅ Payments between dates (all businesses)
    @Query("""
        SELECT p
        FROM BillPayment p
        WHERE p.bill.billDate BETWEEN :start AND :end
    """)
    List<BillPayment> findPaymentsBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // ✅ Payments between dates for a specific business
    @Query("""
        SELECT p
        FROM BillPayment p
        WHERE p.bill.billDate BETWEEN :start AND :end
          AND p.bill.business.id = :businessId
    """)
    List<BillPayment> findPaymentsBetweenByBusiness(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("businessId") Long businessId
    );

    // ✅ All payments for a business (only paid bills)
    List<BillPayment> findByBill_Business_Id(Long businessId);

    // ✅ ⭐ MAIN REQUIREMENT:
    // Fetch ALL BILLS of a business where otherAmount > 0
    @Query("""
        SELECT p.bill
        FROM BillPayment p
        WHERE p.amountToBeCollected > 0
          AND p.bill.business.id = :businessId
    """)
    List<BillEntity> findBillsWithOtherAmountByBusiness(
            @Param("businessId") Long businessId
    );
}
