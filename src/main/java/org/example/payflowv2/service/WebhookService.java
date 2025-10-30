package org.example.payflowv2.service;

import lombok.RequiredArgsConstructor;
import org.example.payflowv2.model.Merchant;
import org.example.payflowv2.model.Transaction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final RestTemplate restTemplate = new RestTemplate();

    public void notifyMerchantAsync(Merchant merchant, Transaction tx) {
        String url = merchant.getWebhookUrl();
        if (url == null || url.isBlank()) return;

        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("transactionId", tx.getPublicId());
                payload.put("status", tx.getStatus().name());
                payload.put("amount", tx.getAmount());
                payload.put("currency", tx.getCurrency());
                payload.put("customerEmail", tx.getCustomerEmail());
                payload.put("createdAt", tx.getCreatedAt());
                if (tx.getFailureReason() != null) payload.put("failureReason", tx.getFailureReason());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                restTemplate.postForEntity(url, entity, Void.class);
            } catch (Exception ignored) {
                // Swallow exceptions to avoid impacting payment flow in demo
            }
        });
    }
}
