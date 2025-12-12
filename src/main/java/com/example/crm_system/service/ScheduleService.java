package com.example.crm_system.service;

import com.example.crm_system.dto.ScheduleRequest;
import com.example.crm_system.entity.BillEntity;
import com.example.crm_system.entity.ScheduleEntity;
import com.example.crm_system.entity.ScheduleProductEntity;
import com.example.crm_system.entity.ScheduleServiceEntity;
import com.example.crm_system.repository.BillRepository;
import com.example.crm_system.repository.BusinessRepository;
import com.example.crm_system.repository.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final BusinessRepository businessRepository;
    private final BillRepository billRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, 
                           BusinessRepository businessRepository,
                           BillRepository billRepository) {
        this.scheduleRepository = scheduleRepository;
        this.businessRepository = businessRepository;
        this.billRepository = billRepository;
    }
public ScheduleEntity saveForLater(ScheduleRequest req) {
    var business = businessRepository.findById(req.getBusinessId())
            .orElseThrow(() -> new RuntimeException("Business not found"));

    ScheduleEntity schedule = new ScheduleEntity();
    schedule.setBusiness(business);
    schedule.setCustomerName(req.getCustomerName());
        schedule.setStaffName(req.getStaffName());

    schedule.setPhoneNumber(req.getPhoneNumber());
    schedule.setCustomerGender(req.getCustomerGender());

    // ✅ Map services
    List<ScheduleServiceEntity> serviceEntities = req.getServices().stream()
            .map(dto -> {
                ScheduleServiceEntity s = new ScheduleServiceEntity();
                s.setName(dto.getName());
                s.setPrice(dto.getPrice());
                s.setQuantity(dto.getQuantity());
                s.setDuration(dto.getDuration());
                s.setSchedule(schedule);
                return s;
            }).toList();

    // ✅ Map products
    List<ScheduleProductEntity> productEntities = req.getProducts().stream()
            .map(dto -> {
                ScheduleProductEntity p = new ScheduleProductEntity();
                p.setName(dto.getName());
                p.setPrice(dto.getPrice());
                p.setQuantity(dto.getQuantity());
                p.setSchedule(schedule);
                return p;
            }).toList();

    schedule.setServices(serviceEntities);
    schedule.setProducts(productEntities);
    schedule.setStatus("PENDING");
    schedule.setAppointmentDateTime(req.getAppointmentDateTime());


    return scheduleRepository.save(schedule);
}


    public List<ScheduleEntity> getByBusiness(Long businessId) {
        return scheduleRepository.findByBusinessId(businessId);
    }

    public List<ScheduleEntity> getByPhone(String phoneNumber) {
        return scheduleRepository.findByPhoneNumber(phoneNumber);
    }

    public ScheduleEntity linkBill(Long scheduleId, Long billId) {
        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        BillEntity bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        schedule.setBill(bill);
        schedule.setStatus("COMPLETED");

        return scheduleRepository.save(schedule);
    }
   public ScheduleEntity updateSchedule(Long businessId, Long scheduleId, ScheduleRequest req) {
    ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
        .orElseThrow(() -> new RuntimeException("Schedule not found"));

    if (!schedule.getBusiness().getId().equals(businessId)) {
        throw new RuntimeException("Schedule does not belong to this business");
    }

    // ✅ Only update if provided
    if (req.getCustomerName() != null) {
        schedule.setCustomerName(req.getCustomerName());
    }
    if (req.getPhoneNumber() != null) {
        schedule.setPhoneNumber(req.getPhoneNumber());
    }
    if (req.getCustomerGender() != null) {
        schedule.setCustomerGender(req.getCustomerGender());
    }
    if (req.getAppointmentDateTime() != null) {
        schedule.setAppointmentDateTime(req.getAppointmentDateTime());
    }

    // ✅ Update services only if provided
    if (req.getServices() != null && !req.getServices().isEmpty()) {
        schedule.getServices().clear();
        List<ScheduleServiceEntity> serviceEntities = req.getServices().stream()
            .map(dto -> {
                ScheduleServiceEntity s = new ScheduleServiceEntity();
                s.setName(dto.getName());
                s.setPrice(dto.getPrice());
                s.setQuantity(dto.getQuantity());
                s.setDuration(dto.getDuration());
                s.setSchedule(schedule);
                return s;
            }).toList();
        schedule.getServices().addAll(serviceEntities);
    }

    return scheduleRepository.save(schedule);
}
public void deleteSchedule(Long businessId, Long scheduleId) {
    ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Schedule not found"));

    if (!schedule.getBusiness().getId().equals(businessId)) {
        throw new RuntimeException("Schedule does not belong to this business");
    }
schedule.getServices().clear();
    schedule.getProducts().clear();
    scheduleRepository.delete(schedule);

    System.out.println("✅ Schedule deleted: ID = " + scheduleId + ", Business ID = " + businessId);
}


}
