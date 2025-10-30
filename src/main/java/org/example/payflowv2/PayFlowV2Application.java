package org.example.payflowv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;

import org.example.payflowv2.repository.MerchantRepository;
import org.example.payflowv2.service.MerchantService;

@SpringBootApplication
public class PayFlowV2Application {

    public static void main(String[] args) {
        SpringApplication.run(PayFlowV2Application.class, args);
    }

    @Bean
    ApplicationRunner seedDemo(MerchantRepository merchantRepository, MerchantService merchantService){
        return args -> {
            merchantRepository.findByEmail("demo@merchant.test").orElseGet(() -> {
                return merchantService.registerMerchant("Demo Store", "demo@merchant.test", "demo1234");
            });
        };
    }
}
