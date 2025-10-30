package org.example.payflowv2.controller;

import lombok.RequiredArgsConstructor;
import org.example.payflowv2.model.Merchant;
import org.example.payflowv2.repository.MerchantRepository;
import org.example.payflowv2.repository.TransactionRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PortalController {
    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;

    private Merchant currentMerchant(User user) {
        return merchantRepository.findByEmail(user.getUsername()).orElseThrow();
    }

    @GetMapping("/portal/dashboard")
    public String dashboard(@AuthenticationPrincipal User user, Model model) {
        Merchant merchant = currentMerchant(user);
        model.addAttribute("merchant", merchant);
        model.addAttribute("transactions", transactionRepository.findByMerchantOrderByCreatedAtDesc(merchant));
        return "portal/dashboard";
    }

    @GetMapping("/portal/settings")
    public String settings(@AuthenticationPrincipal User user, Model model) {
        Merchant merchant = currentMerchant(user);
        model.addAttribute("merchant", merchant);
        return "portal/settings";
    }

    @PostMapping("/portal/settings")
    public String saveSettings(@AuthenticationPrincipal User user,
                               @RequestParam(name = "webhookUrl", required = false) String webhookUrl,
                               Model model) {
        Merchant merchant = currentMerchant(user);
        merchant.setWebhookUrl((webhookUrl != null && !webhookUrl.isBlank()) ? webhookUrl.trim() : null);
        merchantRepository.save(merchant);
        return "redirect:/portal/settings?saved";
    }
}
