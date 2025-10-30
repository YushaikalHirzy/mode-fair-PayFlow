package org.example.payflowv2.repository;

import org.example.payflowv2.model.Merchant;
import org.example.payflowv2.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByPublicId(String publicId);
    List<Transaction> findByMerchantOrderByCreatedAtDesc(Merchant merchant);
}
