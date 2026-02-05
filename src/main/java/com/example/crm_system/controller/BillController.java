package com.example.crm_system.controller;

import com.example.crm_system.dto.BillDetailsResponseDTO;
import com.example.crm_system.dto.BillRequest;
import com.example.crm_system.dto.CustomerTransactionSummary;
import com.example.crm_system.entity.BillEntity;
import com.example.crm_system.service.BillService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "*")

public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping("/generatebill")
    public BillEntity createBill(@RequestBody BillRequest request) {
        return billService.createBill(request);
    }

    // ✅ Get bills by business id
    @GetMapping("/business/{businessId}")
    public List<BillEntity> getBillsByBusiness(@PathVariable Long businessId) {
        return billService.getBillsByBusiness(businessId);
    }
 @GetMapping("/business/{businessId}/rep")
public List<BillDetailsResponseDTO> getBillsByBusinessrep(
        @PathVariable Long businessId) {
    return billService.getBillsByBusinessrep(businessId);
}


    // ✅ Get bills by customer phone number
    @GetMapping("/customer/{phoneNumber}")
    public List<BillEntity> getBillsByCustomer(@PathVariable String phoneNumber) {
        return billService.getBillsByCustomerPhone(phoneNumber);
    }
    @GetMapping("/customer-summary/{phoneNumber}")
public CustomerTransactionSummary getCustomerSummary(@PathVariable String phoneNumber) {
    return billService.getCustomerSummary(phoneNumber);
}

}
