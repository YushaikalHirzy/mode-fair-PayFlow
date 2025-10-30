package org.example.payflowv2.service;

import lombok.RequiredArgsConstructor;
import org.example.payflowv2.model.Merchant;
import org.example.payflowv2.repository.MerchantRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class MerchantService implements UserDetailsService {
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Merchant not found"));
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_MERCHANT");
        return new User(merchant.getEmail(), merchant.getPasswordHash(), Collections.singleton(authority));
    }

    @Transactional
    public Merchant registerMerchant(String name, String email, String rawPassword) {
        if (merchantRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        String apiKey = generateApiKey();
        Merchant merchant = Merchant.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .apiKey(apiKey)
                .build();
        return merchantRepository.save(merchant);
    }

    public String generateApiKey() {
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        // URL-safe base64 without padding
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    public Merchant findByApiKeyOrThrow(String apiKey) {
        return merchantRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new IllegalArgumentException("Invalid API key"));
    }
}
