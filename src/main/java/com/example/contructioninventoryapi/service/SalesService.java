package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.dto.SalesRequest;
import com.example.contructioninventoryapi.entity.*;
import com.example.contructioninventoryapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SalesService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductService productService;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private BranchRepository branchRepository;
    @Autowired private PaymentRepository paymentRepository;

    @Transactional
    public Order createSale(SalesRequest request) {

        // --- 1. CREATE ORDER HEADER ---
        Order order = new Order();
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found: " + request.getBranchId()));
        order.setBranch(branch);

        order.setOrderDate(LocalDateTime.now());
        order.setCreatedBy(request.getCreatedBy());
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);

        if (request.getCustomerId() != null && !request.getCustomerId().isEmpty()) {
            Customer customer = customerRepository.findById(request.getCustomerId()).orElse(null);
            order.setCustomer(customer);
        }

        Order savedOrder = orderRepository.save(order);
        BigDecimal itemsTotal = BigDecimal.ZERO;

        // --- 2. PROCESS SALE ITEMS ---
        for (SalesRequest.SalesItemDto itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDto.getProductId()));

            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setProduct(product);
            detail.setQuantity(itemDto.getQuantity());
            detail.setPriceAtSale(itemDto.getUnitPrice());
            detail.setCostAtSale(product.getCostPrice());

            BigDecimal itemDiscount = itemDto.getDiscount() != null ? itemDto.getDiscount() : BigDecimal.ZERO;
            detail.setDiscount(itemDiscount);

            // Calculate Line Total: (Price * Qty) - Discount
            BigDecimal lineTotal = itemDto.getUnitPrice()
                    .multiply(new BigDecimal(itemDto.getQuantity()))
                    .subtract(itemDiscount);

            detail.setSubtotal(lineTotal);
            itemsTotal = itemsTotal.add(lineTotal);

            orderDetailRepository.save(detail);

            // Deduct Stock immediately (Assuming sale is confirmed)
            productService.deductStock(request.getBranchId(), product.getProductId(), itemDto.getQuantity());
        }

        // --- 3. CALCULATE GRAND TOTAL & DETERMINE PAYMENT STATUS ---
        BigDecimal grandTotal = itemsTotal.subtract(savedOrder.getDiscountAmount());
        savedOrder.setTotalAmount(grandTotal);

        BigDecimal amountPaid = request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO;

        // Auto-assign Status based on Payment
        if (amountPaid.compareTo(grandTotal) >= 0) {
            savedOrder.setPaymentStatus("Paid");
            savedOrder.setStatus("Completed"); // Close sale completely
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            savedOrder.setPaymentStatus("Partial");
            savedOrder.setStatus("Pending"); // Keep pending until fully paid
        } else {
            savedOrder.setPaymentStatus("Unpaid");
            savedOrder.setStatus("Pending");
        }

        orderRepository.save(savedOrder);

        // --- 4. GENERATE INVOICE (Moved up before reloading) ---
        Invoice savedInvoice = createInvoiceForOrder(savedOrder);

        // --- 5. PROCESS INITIAL PAYMENT (If any) ---
        if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            createPaymentForInvoice(savedInvoice, amountPaid, request.getPaymentMethod(), null);
        }

        // --- 6. RELOAD FINAL DATA ---
        Order finalOrder = orderRepository.findById(savedOrder.getOrderId())
                .orElseThrow(() -> new RuntimeException("Error reloading order"));

        // Wake up lazy loaded lists before returning to React
        if (finalOrder.getOrderDetails() != null) {
            finalOrder.getOrderDetails().size();
        }
        if (finalOrder.getInvoice() != null && finalOrder.getInvoice().getPayments() != null) {
            finalOrder.getInvoice().getPayments().size();
        }

        return finalOrder;
    }

    // =================================================================================
    // NEW METHOD: Add a later payment to an existing partial/unpaid Order
    // =================================================================================
    @Transactional
    public Invoice addPayment(String invoiceId, BigDecimal amountToPay, String paymentMethod, String transactionRef) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        Order order = invoice.getOrder();

        // 1. Create the new Payment record
        createPaymentForInvoice(invoice, amountToPay, paymentMethod, transactionRef);

        // 2. Calculate the total paid so far for this invoice
        BigDecimal totalPaidSoFar = invoice.getPayments().stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(amountToPay); // Add the current payment we just created

        BigDecimal grandTotal = order.getTotalAmount();

        // 3. Update Order Status if the balance is fully paid
        if (totalPaidSoFar.compareTo(grandTotal) >= 0) {
            order.setPaymentStatus("Paid");
            order.setStatus("Completed"); // Close the sale
        } else {
            order.setPaymentStatus("Partial");
        }

        orderRepository.save(order);

        // Return updated Invoice
        return invoiceRepository.findById(invoiceId).orElse(invoice);
    }

    // =================================================================================
    // FETCH ALL SALES (Fixed Lazy Loading)
    // =================================================================================
    @Transactional(readOnly = true)
    public List<Order> getAllSales(String branchId) {
        List<Order> sales;

        // THE FIX: Catch literal "null" and "undefined" strings from React's localStorage
        if (branchId == null || branchId.trim().isEmpty() || branchId.equals("null") || branchId.equals("undefined")) {
            sales = orderRepository.findAll(); // If no valid branch, return all sales
        } else {
            sales = orderRepository.findByBranchBranchIdOrderByOrderDateDesc(branchId);
        }

        // Force Hibernate to fetch the nested lists
        for (Order sale : sales) {
            if (sale.getOrderDetails() != null) {
                sale.getOrderDetails().size();
            }
            if (sale.getInvoice() != null && sale.getInvoice().getPayments() != null) {
                sale.getInvoice().getPayments().size();
            }
        }

        return sales;
    }

    // =================================================================================
    // HELPERS
    // =================================================================================

    private Invoice createInvoiceForOrder(Order order) {
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setBranch(order.getBranch());
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setGrandTotal(order.getTotalAmount());

        // Set Due Date logic
        if ("Unpaid".equalsIgnoreCase(order.getPaymentStatus()) || "Partial".equalsIgnoreCase(order.getPaymentStatus())) {
            invoice.setDueDate(LocalDateTime.now().plusDays(30));
        } else {
            invoice.setDueDate(LocalDateTime.now());
        }

        return invoiceRepository.save(invoice);
    }

    private void createPaymentForInvoice(Invoice invoice, BigDecimal amount, String method, String transactionRef) {
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmountPaid(amount);
        payment.setPaymentMethod(method != null ? method : "Cash");
        payment.setTransactionRef(transactionRef);

        paymentRepository.save(payment);
    }
}