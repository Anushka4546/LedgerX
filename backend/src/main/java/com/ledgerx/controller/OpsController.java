package com.ledgerx.controller;

import com.ledgerx.dto.OpsResponse;
import com.ledgerx.service.EvidenceService;
import com.ledgerx.service.TruthResolutionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ops")
public class OpsController {

    private final EvidenceService evidenceService;
    private final TruthResolutionService truthService;

    public OpsController(EvidenceService evidenceService,
                         TruthResolutionService truthService) {
        this.evidenceService = evidenceService;
        this.truthService = truthService;
    }

    @GetMapping("/transaction/{txnId}")
    public OpsResponse inspect(@PathVariable String txnId) {

        var evidence = evidenceService.getEvidenceByTransactionId(txnId);
        String status = truthService.resolve(evidence);

        return new OpsResponse(txnId, status, evidence);
    }
}

