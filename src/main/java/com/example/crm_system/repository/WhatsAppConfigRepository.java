package com.example.crm_system.repository;

import com.example.crm_system.entity.WhatsAppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WhatsAppConfigRepository extends JpaRepository<WhatsAppConfig, Long> {
    
    Optional<WhatsAppConfig> findByBusinessId(Long businessId);
    
    // For single business setup, find the first configuration
    Optional<WhatsAppConfig> findFirstByOrderByIdAsc();
}