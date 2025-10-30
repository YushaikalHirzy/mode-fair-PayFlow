package org.example.payflowv2.controller;

import lombok.RequiredArgsConstructor;
import org.example.payflowv2.model.Merchant;
import org.example.payflowv2.repository.MerchantRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class DemoController {
    private final MerchantRepository merchantRepository;

    @GetMapping({"/", "/demo"})
    public String demoHome(Model model) {
        // If a demo merchant exists, pass its API key to the page for convenience
        Optional<Merchant> demo = merchantRepository.findByEmail("demo@merchant.test");
        model.addAttribute("demoApiKey", demo.map(Merchant::getApiKey).orElse(""));
        return "demo/index";
    }

    @GetMapping("/demo/confirmation")
    public String confirmation(@RequestParam("tid") String transactionId, Model model) {
        model.addAttribute("transactionId", transactionId);
        return "demo/confirmation";
    }
}
