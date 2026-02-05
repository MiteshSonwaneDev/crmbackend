package com.example.crm_system.service;

import com.example.crm_system.dto.BillDetailsResponseDTO;
import com.example.crm_system.dto.BillItemDTO;
import com.example.crm_system.dto.BillPaymentResponseDTO;
import com.example.crm_system.dto.BillRequest;
import com.example.crm_system.dto.CustomerTransactionSummary;
import com.example.crm_system.dto.ItemRequest;
import com.example.crm_system.entity.BillEntity;
import com.example.crm_system.entity.BillItem;
import com.example.crm_system.entity.BillPayment;
import com.example.crm_system.entity.Business;
import com.example.crm_system.repository.BillRepository;
import com.example.crm_system.repository.BusinessRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final BusinessRepository businessRepository;

    public BillService(BillRepository billRepository, BusinessRepository businessRepository) {
        this.billRepository = billRepository;
        this.businessRepository = businessRepository;
    }
public List<BillEntity> getBillsByBusiness(Long businessId) {
    return billRepository.findByBusinessId(businessId);
}
public List<BillDetailsResponseDTO> getBillsByBusinessrep(Long businessId) {

    List<BillEntity> bills = billRepository.findByBusinessId(businessId);

    return bills.stream().map(this::mapToBillDetailsDTO).toList();
}
private BillDetailsResponseDTO mapToBillDetailsDTO(BillEntity bill) {

    BillDetailsResponseDTO dto = new BillDetailsResponseDTO();

    dto.setBillId(bill.getId());
    dto.setBillNumber(bill.getBillNumber());
    dto.setBillDate(bill.getBillDate());

    dto.setCustomerName(bill.getCustomerName());
    dto.setPhoneNumber(bill.getPhoneNumber());
    dto.setCustomerGender(bill.getCustomerGender());

    dto.setServiceTotal(bill.getServiceTotal());
    dto.setProductTotal(bill.getProductTotal());
    dto.setDiscount(bill.getDiscount());
    dto.setGst(bill.getGst());
    dto.setNetPayable(bill.getNetPayable());

    // ✅ Items
    dto.setItems(
        bill.getItems().stream().map(item -> {
            BillItemDTO itemDTO = new BillItemDTO();
            itemDTO.setName(item.getName());
            itemDTO.setType(item.getItemType());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setRate(item.getPrice());
            itemDTO.setDescription(item.getDescription());
            itemDTO.setDuration(item.getDuration());
            return itemDTO;
        }).toList()
    );

    // ✅ Payment (optional)
    if (bill.getPayment() != null) {
        dto.setPayment(mapToPaymentDTO(bill));
    }

    return dto;
}
private BillPaymentResponseDTO mapToPaymentDTO(BillEntity bill) {

    BillPayment payment = bill.getPayment();

    BillPaymentResponseDTO dto = new BillPaymentResponseDTO();

    dto.setPaymentId(payment.getId());
    dto.setBillId(bill.getId());
    dto.setBusinessId(bill.getBusiness().getId()); // ID only (lightweight)

    dto.setCustomerName(bill.getCustomerName());
    dto.setPhoneNumber(bill.getPhoneNumber());
    dto.setCustomerGender(bill.getCustomerGender());
    dto.setBillDate(bill.getBillDate());

    dto.setCashAmount(payment.getCashAmount());
    dto.setCardAmount(payment.getCardAmount());
    dto.setOtherAmount(payment.getOtherAmount());
    dto.setUserBalanceAmount(payment.getUserBalanceAmount());
    dto.setCollectionNotes(payment.getCollectionNotes());
    dto.setCustomerNotes(payment.getCustomerNotes());
    dto.setTotalPaid(payment.getTotalPaid());
    dto.setAmountToBeCollected(payment.getAmountToBeCollected());

    return dto;
}


public List<BillEntity> getBillsByCustomerPhone(String phoneNumber) {
    return billRepository.findByPhoneNumber(phoneNumber);
}


    public BillEntity createBill(BillRequest request) {
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        double serviceTotal = request.getServices().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        double productTotal = request.getProducts().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        double subTotal = serviceTotal + productTotal;

      double discountAmount = request.getDiscount(); // always flat ₹

        double gstAmount = (subTotal - discountAmount) * (request.getGst() / 100);

        double netPayable = subTotal - discountAmount + gstAmount;

        // Build bill entity
        BillEntity bill = new BillEntity();
        bill.setBillNumber(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    bill.setBillDate(request.getBillDate() != null ? request.getBillDate() : LocalDateTime.now());
        bill.setServiceTotal(serviceTotal);
        bill.setProductTotal(productTotal);
        bill.setDiscount(request.getDiscount());
        bill.setGst(request.getGst());
        bill.setNetPayable(netPayable);
        bill.setCustomerName(request.getCustomerName());
        bill.setStaffName(request.getStaffName());
        bill.setPhoneNumber(request.getPhoneNumber());
        bill.setCustomerGender(request.getCustomerGender());
        bill.setBusiness(business);

        // Map bill items
        List<BillItem> items = request.getServices().stream()
                .map(s -> toBillItem(s, "SERVICE", bill))
                .collect(Collectors.toList());

        items.addAll(request.getProducts().stream()
                .map(p -> toBillItem(p, "PRODUCT", bill))
                .collect(Collectors.toList()));

        bill.setItems(items);

       BillEntity savedBill = billRepository.save(bill);

    // ✅ Print Bill ID and Number in console
    System.out.println("✅ Bill Created: ID = " + savedBill.getId() +
                       ", Bill Number = " + savedBill.getBillNumber());

    return savedBill;
        
    }

    private BillItem toBillItem(ItemRequest req, String type, BillEntity bill) {
        BillItem item = new BillItem();
        item.setItemType(type);
        item.setName(req.getName());
        item.setPrice(req.getPrice());
        item.setQuantity(req.getQuantity());
         if ("SERVICE".equalsIgnoreCase(type)) {
        item.setDuration(req.getDuration()); // only for services
    } else if ("PRODUCT".equalsIgnoreCase(type)) {
        item.setDescription(req.getDescription()); // ✅ set product description
    }
        item.setBill(bill);
        return item;
    }

    public CustomerTransactionSummary getCustomerSummary(String phoneNumber) {
    List<BillEntity> bills = billRepository.findByPhoneNumber(phoneNumber);

    if (bills.isEmpty()) {
        throw new RuntimeException("No bills found for this customer");
    }

    int totalTransactions = bills.size();
    double totalBusiness = bills.stream().mapToDouble(BillEntity::getNetPayable).sum();
    double averageAppointmentCost = totalBusiness / totalTransactions;

    // collect service and product names
    List<String> services = bills.stream()
            .flatMap(b -> b.getItems().stream()
                    .filter(i -> "SERVICE".equalsIgnoreCase(i.getItemType()))
                    .map(i -> i.getName()))
            .distinct()
            
            .collect(Collectors.toList());

    List<String> products = bills.stream()
            .flatMap(b -> b.getItems().stream()
                    .filter(i -> "PRODUCT".equalsIgnoreCase(i.getItemType()))
                    .map(i -> i.getName()))
            .distinct()
            .collect(Collectors.toList());

    // collect payment details
    List<CustomerTransactionSummary.PaymentDetail> payments = bills.stream()
            .map(b -> {
                return b.getPayment() != null ? mapToPaymentDetail(b.getPayment()) : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    CustomerTransactionSummary summary = new CustomerTransactionSummary();
    summary.setCustomerName(bills.get(0).getCustomerName());
    summary.setPhoneNumber(phoneNumber);
    summary.setTotalTransactions(totalTransactions);
    summary.setTotalBusiness(totalBusiness);
    summary.setAverageAppointmentCost(averageAppointmentCost);
    summary.setServices(services);
    summary.setProducts(products);
    summary.setPayments(payments);

    return summary;
}

private CustomerTransactionSummary.PaymentDetail mapToPaymentDetail(BillPayment payment) {
    CustomerTransactionSummary.PaymentDetail dto = new CustomerTransactionSummary.PaymentDetail();
    dto.setBillId(payment.getBill().getId());
    dto.setTotalPaid(payment.getTotalPaid());
    dto.setAmountToBeCollected(payment.getAmountToBeCollected());
    dto.setCashAmount(payment.getCashAmount());
    dto.setCardAmount(payment.getCardAmount());
    dto.setOtherAmount(payment.getOtherAmount());
    dto.setUserBalanceAmount(payment.getUserBalanceAmount());
    return dto;
}

}
