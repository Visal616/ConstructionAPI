package com.example.contructioninventoryapi.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchasePaymentRequest {
    private BigDecimal amountToPay;
    private String paymentMethod;
    private String transactionRef;
}