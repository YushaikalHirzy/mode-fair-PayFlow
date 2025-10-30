package org.example.payflowv2.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotNull
    @DecimalMin(value = "0.50")
    private BigDecimal amount;

    private String currency = "USD";

    @Email
    @NotBlank
    private String customerEmail;

    @NotBlank
    private String customerName;

    @NotBlank
    @Pattern(regexp = "^[0-9\s-]{12,23}$", message = "Card number must contain digits, spaces, or hyphens")
    private String cardNumber;

    @NotBlank
    private String expiry; // MM/YY

    @NotBlank
    @Pattern(regexp = "\\d{3}")
    private String cvv;

    @NotBlank
    private String country;

    @NotBlank
    private String postcode;
}
