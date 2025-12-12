package com.example.crm_system.repository;

import com.example.crm_system.entity.BillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<BillEntity, Long> {

  @Query("SELECT b FROM BillEntity b WHERE b.billDate BETWEEN :startDate AND :endDate AND b.business.id = :businessId")
List<BillEntity> findBillsBetweenByBusiness(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("businessId") Long businessId
);



    List<BillEntity> findByBusinessId(Long businessId);

    // 🔹 Find bills by customer phone number (assuming BillEntity has a field "phoneNumber")
    List<BillEntity> findByPhoneNumber(String phoneNumber);

    // 🔹 Custom query for date range (ReportService needs this)
    List<BillEntity> findByBusinessIdAndPhoneNumber(Long businessId, String phoneNumber);
        Optional<BillEntity> findByIdAndBusinessId(Long id, Long businessId);

   
}
