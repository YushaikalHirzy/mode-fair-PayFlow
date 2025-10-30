package org.example.payflowv2.service;

import lombok.RequiredArgsConstructor;
import org.example.payflowv2.model.Merchant;
import org.example.payflowv2.model.Transaction;
import org.example.payflowv2.model.TransactionStatus;
import org.example.payflowv2.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final TransactionRepository transactionRepository;
    private final WebhookService webhookService;

    @Transactional
    public Transaction processPayment(Merchant merchant,
                                      BigDecimal amount,
                                      String currency,
                                      String customerEmail,
                                      String customerName,
                                      String cardNumber,
                                      String expiry,
                                      String cvv,
                                      String country,
                                      String postcode) {

        // Create transaction in PENDING state
        Transaction tx = Transaction.builder()
                .publicId(UUID.randomUUID().toString().replaceAll("-", ""))
                .merchant(merchant)
                .amount(amount)
                .currency(currency == null ? "USD" : currency.toUpperCase())
                .status(TransactionStatus.PENDING)
                .customerEmail(customerEmail)
                .customerName(customerName)
                .country(country)
                .postcode(postcode)
                .build();
        tx = transactionRepository.save(tx);

        // Simulate payment rules: success only if cardNumber == 4242... and cvv length 3 and expiry format MM/YY and not expired (basic check)
        // Normalize inputs
        String digitsCard = sanitizeCard(cardNumber);
        boolean cardOk = digitsCard != null && digitsCard.equals("4242424242424242");
        boolean cvvOk = cvv != null && cvv.matches("\\d{3}");
        boolean expiryOk = isExpiryValid(expiry);

        if (cardOk && cvvOk && expiryOk) {
            tx.setStatus(TransactionStatus.SUCCESS);
            if (digitsCard != null && digitsCard.length() >= 4) {
                tx.setCardLast4(digitsCard.substring(digitsCard.length() - 4));
            }
        } else {
            tx.setStatus(TransactionStatus.FAILED);
            if (!cardOk) tx.setFailureReason("Invalid card number (use 4242 4242 4242 4242 test card)");
            else if (!cvvOk) tx.setFailureReason("Invalid CVV (must be 3 digits)");
            else if (!expiryOk) tx.setFailureReason("Card expired or invalid expiry (use MM/YY or MM/YYYY)");
            else tx.setFailureReason("Card was declined (test rule)");
        }
        tx = transactionRepository.save(tx);

        // Fire webhook if set (non-blocking)
        webhookService.notifyMerchantAsync(merchant, tx);

        return tx;
    }

    private boolean isExpiryValid(String expiry) {
        if (expiry == null) return false;
        String e = expiry.trim();
        // Accept MM/YY, MM-YY, MM/YYYY, MM-YYYY
        if (!e.matches("\\d{2}[/\\-]\\d{2,4}")) return false;
        int mm = Integer.parseInt(e.substring(0, 2));
        String yearPart = e.substring(3);
        int yy = Integer.parseInt(yearPart.length() == 4 ? yearPart.substring(2) : yearPart);
        if (mm < 1 || mm > 12) return false;
        // consider cards valid until end of month 20yy
        int year = 2000 + yy;
        // naive: if current year > year or (== and current month > mm) then expired
        LocalDateTime now = LocalDateTime.now();
        if (now.getYear() > year) return false;
        if (now.getYear() == year && now.getMonthValue() > mm) return false;
        return true;
    }

    private String sanitizeCard(String cardNumber) {
        if (cardNumber == null) return null;
        String digits = cardNumber.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : digits;
    }
}
