package org.example.payflowv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String status; // SUCCESS or FAILED
    private String transactionId;
    private String message;
}
