package com.example.crm_system.repository;

import com.example.crm_system.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    List<ScheduleEntity> findByBusinessId(Long businessId);
    List<ScheduleEntity> findByPhoneNumber(String phoneNumber);
}
