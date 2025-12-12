package com.example.crm_system.service;

import com.example.crm_system.dto.BillDetailsResponseDTO;
import com.example.crm_system.dto.BillItemDTO;
import com.example.crm_system.dto.BillPaymentResponseDTO;
import com.example.crm_system.dto.CustomerPaymentSummary;
import com.example.crm_system.entity.BillEntity;
import com.example.crm_system.entity.BillPayment;
import com.example.crm_system.entity.CustomerEntity;
import com.example.crm_system.repository.BillPaymentRepository;
import com.example.crm_system.repository.BillRepository;
import com.example.crm_system.repository.CustomerRepository;
import com.example.crm_system.entity.BillItem;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillPaymentService {

    private final BillRepository billRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final CustomerRepository customerRepository;

    // ✅ Include all repositories in constructor
    public BillPaymentService(BillRepository billRepository,
                              BillPaymentRepository billPaymentRepository,
                              CustomerRepository customerRepository) {
        this.billRepository = billRepository;
        this.billPaymentRepository = billPaymentRepository;
        this.customerRepository = customerRepository;
    }
  public BillPayment updatePayment(Long paymentId, BillPayment updatedPayment) {
    BillPayment existingPayment = billPaymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

    if (updatedPayment.getCashAmount() != null)
        existingPayment.setCashAmount(updatedPayment.getCashAmount());
    if (updatedPayment.getCardAmount() != null)
        existingPayment.setCardAmount(updatedPayment.getCardAmount());
    if (updatedPayment.getOtherAmount() != null)
        existingPayment.setOtherAmount(updatedPayment.getOtherAmount());
    if (updatedPayment.getUserBalanceAmount() != null)
        existingPayment.setUserBalanceAmount(updatedPayment.getUserBalanceAmount());
    if (updatedPayment.getCollectionNotes() != null)
        existingPayment.setCollectionNotes(updatedPayment.getCollectionNotes());
    if (updatedPayment.getCustomerNotes() != null)
        existingPayment.setCustomerNotes(updatedPayment.getCustomerNotes());
    if (updatedPayment.getPaymentDate() != null)
        existingPayment.setPaymentDate(updatedPayment.getPaymentDate());

    double totalPaid = (existingPayment.getCashAmount() != null ? existingPayment.getCashAmount() : 0)
            + (existingPayment.getCardAmount() != null ? existingPayment.getCardAmount() : 0)
            + (existingPayment.getOtherAmount() != null ? existingPayment.getOtherAmount() : 0)
            + (existingPayment.getUserBalanceAmount() != null ? existingPayment.getUserBalanceAmount() : 0);

    existingPayment.setTotalPaid(totalPaid);

    BillEntity bill = existingPayment.getBill();
    if (bill != null) {
        double amountToBeCollected = bill.getNetPayable() - totalPaid;
        existingPayment.setAmountToBeCollected(amountToBeCollected < 0 ? 0 : amountToBeCollected);
    }

    return billPaymentRepository.save(existingPayment);
}


    // ✅ Record a payment for a bill
    public BillPayment recordPayment(Long billId, BillPayment payment) {
    BillEntity bill = billRepository.findById(billId)
            .orElseThrow(() -> new RuntimeException("Bill not found"));

    // ✅ Default nulls to 0.0
    double cashAmount = payment.getCashAmount() != null ? payment.getCashAmount() : 0.0;
    double cardAmount = payment.getCardAmount() != null ? payment.getCardAmount() : 0.0;
    double otherAmount = payment.getOtherAmount() != null ? payment.getOtherAmount() : 0.0;
    double userBalanceAmount = payment.getUserBalanceAmount() != null ? payment.getUserBalanceAmount() : 0.0;

    double totalPaid = cashAmount + cardAmount + otherAmount + userBalanceAmount;
    double amountToBeCollected = bill.getNetPayable() - totalPaid;

    payment.setCashAmount(cashAmount);
    payment.setCardAmount(cardAmount);
    payment.setOtherAmount(otherAmount);
    payment.setUserBalanceAmount(userBalanceAmount);
    payment.setTotalPaid(totalPaid);
    payment.setAmountToBeCollected(amountToBeCollected < 0 ? 0.0 : amountToBeCollected);
    payment.setBill(bill);

    return billPaymentRepository.save(payment);
}

    // ✅ Fetch payment details for one bill
    public BillPayment getPaymentDetails(Long billId) {
        return billPaymentRepository.findByBillId(billId)
                .orElseThrow(() -> new RuntimeException("Payment details not found for bill"));
    }

    // ✅ Fetch all payments for a business
    public List<BillPaymentResponseDTO> getPaymentsByBusiness(Long businessId) {
        return billPaymentRepository.findByBill_Business_Id(businessId)
                .stream()
                .map(bp -> {
                    BillEntity bill = bp.getBill();

                    // Pull customer data from authoritative customer table
                    CustomerEntity customer = customerRepository
                            .findByBusinessIdAndCustomerMobileNumber(
                                    businessId, bill.getPhoneNumber())
                            .orElse(null);

                    BillPaymentResponseDTO dto = new BillPaymentResponseDTO();
                    dto.setPaymentId(bp.getId());
                    dto.setBillId(bill.getId());
                    dto.setBusinessId(bill.getBusiness().getId());

                    // ✅ Prefer customer table values if available
                    dto.setCustomerName(customer != null ? customer.getCustomerName() : bill.getCustomerName());
                    dto.setPhoneNumber(bill.getPhoneNumber());
                    dto.setCustomerGender(customer != null ? customer.getCustomerGender() : bill.getCustomerGender());

                    dto.setCashAmount(bp.getCashAmount());
                    dto.setCardAmount(bp.getCardAmount());
                    dto.setOtherAmount(bp.getOtherAmount());
                    dto.setUserBalanceAmount(bp.getUserBalanceAmount());
                    dto.setCollectionNotes(bp.getCollectionNotes());
                    dto.setCustomerNotes(bp.getCustomerNotes());
                    dto.setTotalPaid(bp.getTotalPaid());
                    dto.setAmountToBeCollected(bp.getAmountToBeCollected());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ✅ Fetch all payments by customer phone
    public CustomerPaymentSummary getPaymentsByCustomerPhone(Long businessId, String phoneNumber) {
        List<BillEntity> bills = billRepository.findByBusinessIdAndPhoneNumber(businessId, phoneNumber);

        if (bills.isEmpty()) {
            throw new RuntimeException("No bills found for this customer in this business");
        }

        // ✅ Always get customer from customer table
        CustomerEntity customer = customerRepository
                .findByBusinessIdAndCustomerMobileNumber(businessId, phoneNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found in this business"));

        int totalTransactions = bills.size();
        double totalBusiness = bills.stream().mapToDouble(BillEntity::getNetPayable).sum();

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

       List<CustomerPaymentSummary.PaymentDetail> payments = bills.stream()
    .filter(b -> b.getPayment() != null)
    .map(b -> {
        BillPayment p = b.getPayment();

        CustomerPaymentSummary.PaymentDetail dto = new CustomerPaymentSummary.PaymentDetail();
        dto.setBillId(b.getId());
        dto.setBillDate(b.getBillDate());
        dto.setTotalPaid(p.getTotalPaid());
        dto.setAmountToBeCollected(p.getAmountToBeCollected());
        dto.setCashAmount(p.getCashAmount());
        dto.setCardAmount(p.getCardAmount());
        dto.setOtherAmount(p.getOtherAmount());
        dto.setUserBalanceAmount(p.getUserBalanceAmount());

        // 🔥 ADD THIS → SERVICES FOR THIS SPECIFIC BILL
        List<String> servicesForBill = b.getItems().stream()
                .filter(i -> "SERVICE".equalsIgnoreCase(i.getItemType()))
                .map(BillItem::getName)
                .collect(Collectors.toList());

        dto.setServices(servicesForBill);

        return dto;
    })
    .collect(Collectors.toList());


        // ✅ Use authoritative customer record
        CustomerPaymentSummary summary = new CustomerPaymentSummary();
        summary.setCustomerName(customer.getCustomerName());
        summary.setPhoneNumber(phoneNumber);
        summary.setTotalTransactions(totalTransactions);
        summary.setTotalBusiness(totalBusiness);
        summary.setServices(services);
        summary.setProducts(products);
        summary.setPayments(payments);

        return summary;
    }
    public BillDetailsResponseDTO getBillDetails(Long businessId, Long billId) {
    BillEntity bill = billRepository.findByIdAndBusinessId(billId, businessId)
            .orElseThrow(() -> new RuntimeException("Bill not found"));

    BillPayment payment = billPaymentRepository.findByBillId(billId)
            .orElse(null);

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

    // map items
    List<BillItemDTO> itemDTOs = bill.getItems().stream().map(item -> {
        BillItemDTO i = new BillItemDTO();
        i.setName(item.getName());
        i.setType(item.getItemType());
        i.setQuantity(item.getQuantity());
        i.setRate(item.getPrice());
        i.setDescription(item.getDescription());
        i.setDuration(item.getDuration());
        return i;
    }).collect(Collectors.toList());

    dto.setItems(itemDTOs);

    // map payment if exists
    if (payment != null) {
        BillPaymentResponseDTO paymentDTO = new BillPaymentResponseDTO();
        paymentDTO.setBillId(payment.getBill().getId());
        paymentDTO.setBillDate(payment.getPaymentDate());
        paymentDTO.setTotalPaid(payment.getTotalPaid());
        paymentDTO.setAmountToBeCollected(payment.getAmountToBeCollected());
        paymentDTO.setCashAmount(payment.getCashAmount());
        paymentDTO.setCardAmount(payment.getCardAmount());
        paymentDTO.setOtherAmount(payment.getOtherAmount());
        paymentDTO.setUserBalanceAmount(payment.getUserBalanceAmount());
        dto.setPayment(paymentDTO);
    }

    return dto;
}

}
