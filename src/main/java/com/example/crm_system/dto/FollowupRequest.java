package com.example.crm_system.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FollowupRequest {
    private LocalDateTime followupDateTime;
    private String followupComments;
    private Boolean hotLead;
}
