package com.ledgerx.core.domain;

import java.time.Instant;

public final class Evidence {

    public final EvidenceSource source;
    public final EvidenceType type;
    public final Instant occurredAt;

    public Evidence(EvidenceSource source,
                    EvidenceType type,
                    Instant occurredAt) {
        this.source = source;
        this.type = type;
        this.occurredAt = occurredAt;
    }
}
