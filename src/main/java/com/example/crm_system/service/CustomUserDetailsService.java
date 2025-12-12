package com.example.crm_system.service;

import com.example.crm_system.entity.Business;
import com.example.crm_system.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private BusinessRepository businessRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Business business;

        if (username.contains("@")) {
            business = businessRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        } else {
            business = businessRepository.findByOwnerMobile(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with mobile: " + username));
        }

        return User.builder()
                .username(username)
                .password(business.getPassword())
                .roles("USER")
                .build();
    }
}
