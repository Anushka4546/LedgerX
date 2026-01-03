package com.ledgerx.service;

import com.ledgerx.model.Evidence;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TruthResolutionService {

    private final CorrelationService correlationService;

    public TruthResolutionService(CorrelationService correlationService) {
        this.correlationService = correlationService;
    }

    public String resolve(List<Evidence> evidence) {
        if (correlationService.hasContradictions(evidence)) {
            return "UNSAFE";
        }
        return "SAFE";
    }
}

