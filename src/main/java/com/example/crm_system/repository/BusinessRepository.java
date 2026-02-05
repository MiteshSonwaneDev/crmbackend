package com.example.crm_system.repository;

import com.example.crm_system.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    Optional<Business> findByEmailAndPassword(String email, String password);
    Optional<Business> findByOwnerMobileAndPassword(String ownerMobile, String password);

    Optional<Business> findByEmail(String email);
    Optional<Business> findByOwnerMobile(String ownerMobile);
    
}
