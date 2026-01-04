package com.ledgerx.core.evidence;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class TimeTickEvidence implements Evidence {

    private final String id = UUID.randomUUID().toString();
    private final Instant observedAt;
    private final Map<String, Object> attributes;

    public TimeTickEvidence(Instant observedAt, Map<String, Object> attributes) {
        this.observedAt = observedAt;
        this.attributes = attributes == null ? Collections.emptyMap() : attributes;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public EvidenceType type() {
        return EvidenceType.TIME;
    }

    @Override
    public Instant observedAt() {
        return observedAt;
    }

    @Override
    public EvidenceSource source() {
        return EvidenceSource.TIME;
    }

    @Override
    public double credibility() {
        return 1.0; // time is always true
    }

    @Override
    public Map<String, Object> attributes() {
        return attributes;
    }
}
