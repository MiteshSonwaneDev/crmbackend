package com.example.crm_system.service;

import com.example.crm_system.dto.BillDetailsResponseDTO;
import com.example.crm_system.dto.BillItemDTO;
import com.example.crm_system.dto.BillPaymentResponseDTO;
import com.example.crm_system.dto.BillWithPaymentDTO;
import com.example.crm_system.dto.CustomerPaymentSummary;
import com.example.crm_system.entity.BillEntity;
import com.example.crm_system.entity.BillPayment;
import com.example.crm_system.entity.BillSettlement;
import com.example.crm_system.entity.CustomerEntity;
import com.example.crm_system.repository.BillPaymentRepository;
import com.example.crm_system.repository.BillRepository;
import com.example.crm_system.repository.BillSettlementRepository;
import com.example.crm_system.repository.CustomerRepository;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import com.example.crm_system.entity.BillItem;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillPaymentService {
        private final BillSettlementRepository billSettlementRepository;
    private static final String BASE_DIR = "uploads/invoices";

    private final BillRepository billRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final CustomerRepository customerRepository;


    // ✅ Include all repositories in constructor
    public BillPaymentService(BillRepository billRepository,
                              BillPaymentRepository billPaymentRepository,
                              BillSettlementRepository billSettlementRepository,
                              CustomerRepository customerRepository
                            ) {
        this.billRepository = billRepository;
        this.billPaymentRepository = billPaymentRepository;
        this.billSettlementRepository = billSettlementRepository;
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

    CustomerEntity customer = customerRepository
            .findByBusinessIdAndCustomerMobileNumber(businessId, phoneNumber)
            .orElseThrow(() -> new RuntimeException("Customer not found in this business"));

    int totalTransactions = bills.size();
    double totalBusiness = bills.stream().mapToDouble(BillEntity::getNetPayable).sum();

    // Get unique service and product names for summary
    List<String> services = bills.stream()
            .flatMap(b -> b.getItems().stream()
                    .filter(i -> "SERVICE".equalsIgnoreCase(i.getItemType()))
                    .map(BillItem::getName))
            .distinct()
            .collect(Collectors.toList());

    List<String> products = bills.stream()
            .flatMap(b -> b.getItems().stream()
                    .filter(i -> "PRODUCT".equalsIgnoreCase(i.getItemType()))
                    .map(BillItem::getName))
            .distinct()
            .collect(Collectors.toList());

    // Build payment details with item quantities
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

            // ✅ Map services with quantities
            List<CustomerPaymentSummary.ItemDetail> servicesForBill = b.getItems().stream()
                    .filter(i -> "SERVICE".equalsIgnoreCase(i.getItemType()))
                    .map(item -> {
                        CustomerPaymentSummary.ItemDetail detail = new CustomerPaymentSummary.ItemDetail();
                        detail.setName(item.getName());
                        detail.setQuantity(item.getQuantity());
                        detail.setPrice(item.getPrice());
            detail.setTotal(item.getPrice() * item.getQuantity());
                        return detail;
                    })
                    .collect(Collectors.toList());

            // ✅ Map products with quantities
            List<CustomerPaymentSummary.ItemDetail> productsForBill = b.getItems().stream()
                    .filter(i -> "PRODUCT".equalsIgnoreCase(i.getItemType()))
                    .map(item -> {
                        CustomerPaymentSummary.ItemDetail detail = new CustomerPaymentSummary.ItemDetail();
                        detail.setName(item.getName());
                        detail.setQuantity(item.getQuantity());
                        detail.setPrice(item.getPrice());
            detail.setTotal(item.getPrice() * item.getQuantity());
                        return detail;
                    })
                    .collect(Collectors.toList());

            dto.setServices(servicesForBill);
            dto.setProducts(productsForBill);

            return dto;
        })
        .collect(Collectors.toList());

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


@Transactional
public void settleBill(Long businessId, Long billId) {

    // 1️⃣ Fetch bill
    BillEntity bill = billRepository
            .findByIdAndBusinessId(billId, businessId)
            .orElseThrow(() -> new RuntimeException("Bill not found"));

    // 2️⃣ Fetch payment
    BillPayment payment = billPaymentRepository
            .findByBillId(billId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

    ObjectMapper mapper = new ObjectMapper();

    // 3️⃣ Convert bill items → BillItemDTO list → JSON
    String itemsJson;
    try {
        List<BillItemDTO> itemDTOs =
                bill.getItems().stream()
                    .map(item -> {
                        BillItemDTO dto = new BillItemDTO();
                        dto.setName(item.getName());
                        dto.setType(item.getItemType()); // enum → String
                        dto.setQuantity(item.getQuantity());
                        dto.setRate(item.getPrice());
                        dto.setDescription(item.getDescription());
                        dto.setDuration(item.getDuration());
                        return dto;
                    })
                    .collect(Collectors.toList());

        itemsJson = mapper.writeValueAsString(itemDTOs);

    } catch (Exception e) {
        throw new RuntimeException("Failed to serialize bill items", e);
    }

    // 4️⃣ Build settlement entity
    BillSettlement settlement = new BillSettlement();
    settlement.setBusinessId(businessId);
    settlement.setBillId(billId);
    settlement.setBillNumber(bill.getBillNumber());
    settlement.setBillDate(bill.getBillDate());

    settlement.setCustomerName(bill.getCustomerName());
    settlement.setPhoneNumber(bill.getPhoneNumber());
    settlement.setCustomerGender(bill.getCustomerGender());

    settlement.setServiceTotal(bill.getServiceTotal());
    settlement.setProductTotal(bill.getProductTotal());
    settlement.setDiscount(bill.getDiscount());
    settlement.setGst(bill.getGst());
    settlement.setNetPayable(bill.getNetPayable());

    settlement.setCashAmount(payment.getCashAmount());
    settlement.setCardAmount(payment.getCardAmount());
    settlement.setOtherAmount(payment.getOtherAmount());
    settlement.setTotalPaid(payment.getTotalPaid());
    settlement.setAmountToBeCollected(payment.getAmountToBeCollected());

    // ⚠️ Only keep this if field exists in BillSettlement
    settlement.setUserBalanceAmount(payment.getUserBalanceAmount());

    settlement.setItemsJson(itemsJson);
    settlement.setPaymentDate(payment.getPaymentDate());

    // 5️⃣ Save settlement
    billSettlementRepository.save(settlement);
}

public boolean saveInvoiceImage(
            Long businessId,
            Long billId,
            String base64Image
    ) {
        try {
            // Remove base64 prefix if present
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // 📂 uploads/invoices/{businessId}/{billId}
            Path dirPath = Paths.get(
                    BASE_DIR,
                    businessId.toString(),
                    billId.toString()
            );

            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Path imagePath = dirPath.resolve("invoice.png");
            Files.write(imagePath, imageBytes);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public byte[] getInvoiceImage(Long businessId, Long billId) {
    try {
        Path imagePath = Paths.get(
                BASE_DIR,
                businessId.toString(),
                billId.toString(),
                "invoice.png"
        );

        if (!Files.exists(imagePath)) {
            return null;
        }

        return Files.readAllBytes(imagePath);

    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
 public List<BillWithPaymentDTO> getOtherAmountBillsByBusiness(Long businessId) {

        return billPaymentRepository
                .findBillsWithOtherAmountByBusiness(businessId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private BillWithPaymentDTO mapToDTO(BillEntity bill) {

        BillWithPaymentDTO dto = new BillWithPaymentDTO();

        dto.setId(bill.getId());
        dto.setBillNumber(bill.getBillNumber());
        dto.setBillDate(bill.getBillDate());
        dto.setServiceTotal(bill.getServiceTotal());
        dto.setProductTotal(bill.getProductTotal());
        dto.setDiscount(bill.getDiscount());
        dto.setGst(bill.getGst());
        dto.setNetPayable(bill.getNetPayable());

        dto.setCustomerName(bill.getCustomerName());
        dto.setPhoneNumber(bill.getPhoneNumber());
        dto.setCustomerGender(bill.getCustomerGender());
        dto.setStaffName(bill.getStaffName());

        // ✅ Items (using your BillItemDTO)
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

        // ✅ Payment (using your BillPaymentResponseDTO)
        if (bill.getPayment() != null) {
            BillPaymentResponseDTO paymentDTO = new BillPaymentResponseDTO();

            paymentDTO.setPaymentId(bill.getPayment().getId());
            paymentDTO.setBillId(bill.getId());
            paymentDTO.setBusinessId(bill.getBusiness().getId());

            paymentDTO.setCustomerName(bill.getCustomerName());
            paymentDTO.setPhoneNumber(bill.getPhoneNumber());
            paymentDTO.setCustomerGender(bill.getCustomerGender());
            paymentDTO.setBillDate(bill.getBillDate());

            paymentDTO.setCashAmount(bill.getPayment().getCashAmount());
            paymentDTO.setCardAmount(bill.getPayment().getCardAmount());
            paymentDTO.setOtherAmount(bill.getPayment().getOtherAmount());
            paymentDTO.setUserBalanceAmount(bill.getPayment().getUserBalanceAmount());
            paymentDTO.setCollectionNotes(bill.getPayment().getCollectionNotes());
            paymentDTO.setCustomerNotes(bill.getPayment().getCustomerNotes());
            paymentDTO.setTotalPaid(bill.getPayment().getTotalPaid());
            paymentDTO.setAmountToBeCollected(bill.getPayment().getAmountToBeCollected());

            dto.setPayment(paymentDTO);
        }

        return dto;
    }

}
