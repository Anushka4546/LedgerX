package com.ledgerx.service;

import com.ledgerx.model.Evidence;
import com.ledgerx.repository.EvidenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvidenceService {

    private final EvidenceRepository repository;

    public EvidenceService(EvidenceRepository repository) {
        this.repository = repository;
    }

    public List<Evidence> getEvidenceByTransactionId(String transactionId) {
        return repository.findByTransactionIdOrderByOccurredAtAsc(transactionId);
    }

    public Evidence save(Evidence evidence) {
        return repository.save(evidence);
    }
}

