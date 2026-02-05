package com.example.crm_system.repository;

import com.example.crm_system.entity.BillEntity;
import com.example.crm_system.entity.BillSettlement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository

public interface BillSettlementRepository
        extends JpaRepository<BillSettlement, Long> {

    Optional<BillSettlement> findByBusinessIdAndBillId(Long businessId, Long billId);
}
