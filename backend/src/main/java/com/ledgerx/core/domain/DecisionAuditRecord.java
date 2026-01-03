package com.ledgerx.core.domain;

import com.ledgerx.core.decision.Decision;
import com.ledgerx.core.domain.Evidence;

import java.time.Instant;
import java.util.List;

public final class DecisionAuditRecord {

    public final String decisionId;
    public final Instant decidedAt;

    public final Decision decision;
    public final List<Evidence> evidenceSnapshot;

    public final String policyVersion;
    public final String modelVersion;

    public DecisionAuditRecord(
            String decisionId,
            Instant decidedAt,
            Decision decision,
            List<Evidence> evidenceSnapshot,
            String policyVersion,
            String modelVersion) {

        this.decisionId = decisionId;
        this.decidedAt = decidedAt;
        this.decision = decision;
        this.evidenceSnapshot = evidenceSnapshot;
        this.policyVersion = policyVersion;
        this.modelVersion = modelVersion;
    }
}
