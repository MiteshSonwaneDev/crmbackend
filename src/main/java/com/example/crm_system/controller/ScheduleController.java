package com.example.crm_system.controller;

import com.example.crm_system.dto.ScheduleRequest;
import com.example.crm_system.entity.ScheduleEntity;
import com.example.crm_system.service.ScheduleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    // ✅ Save Appointment (Save for Later from Billing Page)
    @PostMapping("/save")
public ScheduleEntity saveForLater(@RequestBody ScheduleRequest req) {
    return scheduleService.saveForLater(req);
}


    // ✅ Get all schedules for business
    @GetMapping("/business/{businessId}")
    public List<ScheduleEntity> getByBusiness(@PathVariable Long businessId) {
        return scheduleService.getByBusiness(businessId);
    }

    // ✅ Get schedules by phone
    @GetMapping("/customer/{phoneNumber}")
    public List<ScheduleEntity> getByPhone(@PathVariable String phoneNumber) {
        return scheduleService.getByPhone(phoneNumber);
    }

    // ✅ Link Bill when customer pays
    @PutMapping("/{scheduleId}/link-bill/{billId}")
    public ScheduleEntity linkBill(@PathVariable Long scheduleId, @PathVariable Long billId) {
        return scheduleService.linkBill(scheduleId, billId);
    }
    // ✅ Update schedule
@PutMapping("/{businessId}/{scheduleId}")
public ScheduleEntity updateSchedule(
        @PathVariable Long businessId,
        
        @PathVariable Long scheduleId,
        @RequestBody ScheduleRequest req) {
    return scheduleService.updateSchedule(businessId, scheduleId, req);

    

}


// ✅ Delete schedule by businessId and scheduleId
@DeleteMapping("/{businessId}/{scheduleId}")
public void deleteSchedule(
        @PathVariable Long businessId,
        @PathVariable Long scheduleId) {
    scheduleService.deleteSchedule(businessId, scheduleId);
}

}
