package org.example.payflowv2.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.payflowv2.dto.PaymentRequest;
import org.example.payflowv2.dto.PaymentResponse;
import org.example.payflowv2.model.Merchant;
import org.example.payflowv2.model.Transaction;
import org.example.payflowv2.repository.TransactionRepository;
import org.example.payflowv2.service.MerchantService;
import org.example.payflowv2.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    private final MerchantService merchantService;
    private final PaymentService paymentService;
    private final TransactionRepository transactionRepository;

    private Merchant authenticate(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Missing API key");
        }
        return merchantService.findByApiKeyOrThrow(apiKey);
    }

    @PostMapping("/payments")
    public ResponseEntity<?> createPayment(@RequestHeader(name = "X-API-KEY", required = false) String apiKey,
                                           @Valid @RequestBody PaymentRequest request) {
        try {
            Merchant merchant = authenticate(apiKey);
            Transaction tx = paymentService.processPayment(
                    merchant,
                    request.getAmount(),
                    request.getCurrency(),
                    request.getCustomerEmail(),
                    request.getCustomerName(),
                    request.getCardNumber(),
                    request.getExpiry(),
                    request.getCvv(),
                    request.getCountry(),
                    request.getPostcode()
            );
            String message = tx.getFailureReason() == null ? "Payment processed" : tx.getFailureReason();
            return ResponseEntity.ok(new PaymentResponse(tx.getStatus().name(), tx.getPublicId(), message));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> listTransactions(@RequestHeader(name = "X-API-KEY", required = false) String apiKey) {
        try {
            Merchant merchant = authenticate(apiKey);
            return ResponseEntity.ok(transactionRepository.findByMerchantOrderByCreatedAtDesc(merchant)
                    .stream()
                    .map(tx -> Map.of(
                            "transactionId", tx.getPublicId(),
                            "status", tx.getStatus().name(),
                            "amount", tx.getAmount(),
                            "currency", tx.getCurrency(),
                            "createdAt", tx.getCreatedAt()
                    )).toList());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransaction(@RequestHeader(name = "X-API-KEY", required = false) String apiKey,
                                            @PathVariable("id") String publicId) {
        try {
            Merchant merchant = authenticate(apiKey);
            return transactionRepository.findByPublicId(publicId)
                    .filter(tx -> tx.getMerchant().getId().equals(merchant.getId()))
                    .<ResponseEntity<?>>map(tx -> ResponseEntity.ok(Map.of(
                            "transactionId", tx.getPublicId(),
                            "status", tx.getStatus().name(),
                            "amount", tx.getAmount(),
                            "currency", tx.getCurrency(),
                            "createdAt", tx.getCreatedAt(),
                            "customerEmail", tx.getCustomerEmail()
                    )))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Transaction not found")));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }
}
