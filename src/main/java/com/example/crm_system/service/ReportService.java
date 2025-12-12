package com.example.crm_system.service;

import com.example.crm_system.dto.*;
import com.example.crm_system.entity.BillEntity;
import com.example.crm_system.entity.BillItem;
import com.example.crm_system.entity.BillPayment;
import com.example.crm_system.repository.BillPaymentRepository;
import com.example.crm_system.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final BillRepository billRepository;
    private final BillPaymentRepository billPaymentRepository;

    public ReportService(BillRepository billRepository, BillPaymentRepository billPaymentRepository) {
        this.billRepository = billRepository;
        this.billPaymentRepository = billPaymentRepository;
    }

    /**
     * Generate safe date ranges for reports
     */
    private LocalDateTime[] getDateRange(String filter) {
        LocalDate today = LocalDate.now();
        LocalDateTime start;
        LocalDateTime end;

        switch (filter.toLowerCase()) {
            case "today" -> {
                start = today.atStartOfDay();
                end = today.atTime(LocalTime.MAX);
            }
            case "yesterday" -> {
                start = today.minusDays(1).atStartOfDay();
                end = today.minusDays(1).atTime(LocalTime.MAX);
            }
            case "last7days" -> {
                start = today.minusDays(6).atStartOfDay();
                end = today.atTime(LocalTime.MAX);
            }
            case "last30days" -> {
                start = today.minusDays(29).atStartOfDay();
                end = today.atTime(LocalTime.MAX);
            }
            case "thismonth" -> {
                start = today.withDayOfMonth(1).atStartOfDay();
                end = today.atTime(LocalTime.MAX);
            }
            case "thisyear" -> {
                start = today.withDayOfYear(1).atStartOfDay();
                end = today.atTime(LocalTime.MAX);
            }
            default -> {
                start = today.minusDays(89).atStartOfDay();
                end = today.atTime(LocalTime.MAX);
            }
        }

        return new LocalDateTime[]{start, end};
    }

    /**
     * Transaction report filtered by businessId
     */
    public TransactionReportDTO getTransactionReport(String filter, Long businessId) {
        LocalDateTime[] range = getDateRange(filter);
        List<BillPayment> payments = billPaymentRepository.findPaymentsBetweenByBusiness(range[0], range[1], businessId);

        long transactions = payments.size();
        double totalRevenue = payments.stream().mapToDouble(p -> p.getBill().getNetPayable()).sum();
        double cash = payments.stream().mapToDouble(BillPayment::getCashAmount).sum();
        double card = payments.stream().mapToDouble(BillPayment::getCardAmount).sum();
        double others = payments.stream().mapToDouble(BillPayment::getOtherAmount).sum();

        return new TransactionReportDTO(transactions, totalRevenue, cash, card, others);
    }

    /**
     * Collection report filtered by businessId
     */
    public CollectionReportDTO getCollectionReport(String filter, Long businessId) {
        LocalDateTime[] range = getDateRange(filter);
        List<BillPayment> payments = billPaymentRepository.findPaymentsBetweenByBusiness(range[0], range[1], businessId);

        double totalCash = payments.stream().mapToDouble(BillPayment::getCashAmount).sum();
        double totalCard = payments.stream().mapToDouble(BillPayment::getCardAmount).sum();
        double totalOthers = payments.stream().mapToDouble(BillPayment::getOtherAmount).sum();
        double totalUserBalance = payments.stream().mapToDouble(BillPayment::getUserBalanceAmount).sum();

        return new CollectionReportDTO(totalCash, totalCard, totalOthers, totalUserBalance);
    }

    /**
     * Service/Product report filtered by businessId
     */
    public List<ServiceReportDTO> getServiceReport(String filter, Long businessId, String type) {
        LocalDateTime[] range = getDateRange(filter);
        List<BillEntity> bills = billRepository.findBillsBetweenByBusiness(range[0], range[1], businessId);

        Map<String, List<BillItem>> groupedItems = bills.stream()
                .flatMap(b -> b.getItems().stream())
                .filter(i -> type == null || i.getItemType().equalsIgnoreCase(type))
                .collect(Collectors.groupingBy(BillItem::getName));

        double grandTotal = groupedItems.values().stream()
                .flatMap(List::stream)
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        List<ServiceReportDTO> report = new ArrayList<>();
        for (Map.Entry<String, List<BillItem>> entry : groupedItems.entrySet()) {
            long count = entry.getValue().stream().mapToLong(BillItem::getQuantity).sum();
            double totalCost = entry.getValue().stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
            double percent = grandTotal > 0 ? (totalCost / grandTotal) * 100 : 0;
            String category = entry.getValue().get(0).getItemType();
            report.add(new ServiceReportDTO(category, entry.getKey(), count, totalCost, percent));
        }
        return report;
    }

    /**
     * User balance report filtered by businessId
     */
    public List<UserBalanceReportDTO> getUserBalanceReport(String filter, Long businessId) {
        LocalDateTime[] range = getDateRange(filter);
        List<BillPayment> payments = billPaymentRepository.findPaymentsBetweenByBusiness(range[0], range[1], businessId);

        return payments.stream()
                .filter(p -> p.getAmountToBeCollected() > 0)
                .map(p -> new UserBalanceReportDTO(
                        p.getBill().getCustomerName(),
                        p.getBill().getPhoneNumber(),
                        p.getAmountToBeCollected()
                ))
                .collect(Collectors.toList());
    }

    public List<RevenueComparisonDTO> getRevenueComparison(Long businessId, int months) {
    List<RevenueComparisonDTO> results = new ArrayList<>();

    // Get current date
    YearMonth currentMonth = YearMonth.now();

    // Loop over last `months` months
    for (int i = months - 1; i >= 0; i--) {
        YearMonth targetMonth = currentMonth.minusMonths(i);

        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // Fetch bills for this month
        List<BillEntity> bills = billRepository.findBillsBetweenByBusiness(start, end, businessId);
        double monthlyRevenue = bills.stream().mapToDouble(BillEntity::getNetPayable).sum();

        // Fetch last 3 months average (excluding current month)
        double avg3 = 0;
        int count = 0;
        for (int j = 1; j <= 3; j++) {
            YearMonth prev = targetMonth.minusMonths(j);
            LocalDateTime ps = prev.atDay(1).atStartOfDay();
            LocalDateTime pe = prev.atEndOfMonth().atTime(LocalTime.MAX);

            List<BillEntity> prevBills = billRepository.findBillsBetweenByBusiness(ps, pe, businessId);
            double prevRevenue = prevBills.stream().mapToDouble(BillEntity::getNetPayable).sum();

            avg3 += prevRevenue;
            count++;
        }
        avg3 = count > 0 ? avg3 / count : 0;

        results.add(new RevenueComparisonDTO(
                targetMonth.getMonth().toString().substring(0, 3) + ", " + targetMonth.getYear() % 100,
                monthlyRevenue,
                avg3
        ));
    }
    return results;
}

public List<TransactionComparisonDTO> getTransactionComparison(Long businessId, int months) {
    List<TransactionComparisonDTO> results = new ArrayList<>();

    YearMonth currentMonth = YearMonth.now();

    for (int i = months - 1; i >= 0; i--) {
        YearMonth targetMonth = currentMonth.minusMonths(i);

        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // Bills in this month
        List<BillEntity> bills = billRepository.findBillsBetweenByBusiness(start, end, businessId);
        long transactionCount = bills.size();

        // Calculate last 3-month average
        long totalPrevTransactions = 0;
        int count = 0;

        for (int j = 1; j <= 3; j++) {
            YearMonth prev = targetMonth.minusMonths(j);
            LocalDateTime ps = prev.atDay(1).atStartOfDay();
            LocalDateTime pe = prev.atEndOfMonth().atTime(LocalTime.MAX);

            List<BillEntity> prevBills = billRepository.findBillsBetweenByBusiness(ps, pe, businessId);
            totalPrevTransactions += prevBills.size();
            count++;
        }

        double avg3 = count > 0 ? (double) totalPrevTransactions / count : 0;

        results.add(new TransactionComparisonDTO(
                targetMonth.getMonth().toString().substring(0, 3) + ", " + targetMonth.getYear() % 100,
                transactionCount,
                avg3
        ));
    }

    return results;
}

public List<ServicePerformanceDTO> getServicePerformance(Long businessId) {
    // 1. Fetch all bills of this business
    List<BillEntity> bills = billRepository.findByBusinessId(businessId);

    // 2. Aggregate revenue per service
    Map<String, Double> serviceRevenueMap = new HashMap<>();

    for (BillEntity bill : bills) {
        for (BillItem item : bill.getItems()) { // Assuming BillEntity has List<BillItemEntity> items
            String service = item.getName();
            double revenue = item.getPrice() * item.getQuantity();

            serviceRevenueMap.put(service, serviceRevenueMap.getOrDefault(service, 0.0) + revenue);
        }
    }

    // 3. Calculate total revenue
    double totalRevenue = serviceRevenueMap.values().stream().mapToDouble(Double::doubleValue).sum();

    // 4. Sort services by revenue (descending)
    List<Map.Entry<String, Double>> sortedServices = new ArrayList<>(serviceRevenueMap.entrySet());
    sortedServices.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

    // 5. Collect top services until ≥ 80% of revenue is covered
    List<ServicePerformanceDTO> result = new ArrayList<>();
    double cumulative = 0.0;

    for (Map.Entry<String, Double> entry : sortedServices) {
        double percent = (entry.getValue() / totalRevenue) * 100;
        cumulative += percent;

        result.add(new ServicePerformanceDTO(
                entry.getKey(),
                entry.getValue(),
                Math.round(percent * 100.0) / 100.0 // round to 2 decimals
        ));

        if (cumulative >= 80) break;
    }

    return result;
}



}
