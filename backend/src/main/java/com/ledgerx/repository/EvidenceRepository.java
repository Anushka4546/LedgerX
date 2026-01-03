package com.ledgerx.repository;

import com.ledgerx.model.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {

    List<Evidence> findByTransactionIdOrderByOccurredAtAsc(String transactionId);
}

