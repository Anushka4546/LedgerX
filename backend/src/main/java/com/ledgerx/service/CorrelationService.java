package com.ledgerx.service;

import com.ledgerx.model.Evidence;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorrelationService {

    public boolean hasContradictions(List<Evidence> evidenceList) {
        boolean gatewaySuccess = evidenceList.stream()
                .anyMatch(e -> e.getEventType().equals("REFUND_SUCCESS"));

        boolean ledgerMissing = evidenceList.stream()
                .anyMatch(e -> e.getEventType().equals("LEDGER_WRITE_FAILED"));

        return gatewaySuccess && ledgerMissing;
    }
}

