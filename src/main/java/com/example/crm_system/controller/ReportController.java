package com.example.crm_system.controller;

import com.example.crm_system.dto.*;
import com.example.crm_system.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Transaction report for a specific business
     */
    @GetMapping("/transactions")
    public TransactionReportDTO getTransactionReport(
            @RequestParam String filter,
            @RequestParam Long businessId
    ) {
        return reportService.getTransactionReport(filter, businessId);
    }

    /**
     * Collection report for a specific business
     */
    @GetMapping("/collections")
    public CollectionReportDTO getCollectionReport(
            @RequestParam String filter,
            @RequestParam Long businessId
    ) {
        return reportService.getCollectionReport(filter, businessId);
    }

    /**
     * Service/Product report for a specific business
     */
    @GetMapping("/services")
    public List<ServiceReportDTO> getServiceReport(
            @RequestParam String filter,
            @RequestParam Long businessId,
            @RequestParam(required = false) String type // SERVICE / PRODUCT
    ) {
        return reportService.getServiceReport(filter, businessId, type);
    }

    /**
     * User balance report for a specific business
     */
    @GetMapping("/user-balance")
    public List<UserBalanceReportDTO> getUserBalanceReport(
            @RequestParam String filter,
            @RequestParam Long businessId
    ) {
        return reportService.getUserBalanceReport(filter, businessId);
    }


    @GetMapping("/revenue-comparison")
public List<RevenueComparisonDTO> getRevenueComparison(
        @RequestParam Long businessId,
        @RequestParam int months 
) {
    return reportService.getRevenueComparison(businessId, months);
}

@GetMapping("/transaction-comparison")
public List<TransactionComparisonDTO> getTransactionComparison(
        @RequestParam Long businessId,
        @RequestParam int months // e.g. 6 for last 6 months
) {
    return reportService.getTransactionComparison(businessId, months);
}

@GetMapping("/service-performance")
public List<ServicePerformanceDTO> getServicePerformance(
        @RequestParam Long businessId
) {
    return reportService.getServicePerformance(businessId);
}

} 
