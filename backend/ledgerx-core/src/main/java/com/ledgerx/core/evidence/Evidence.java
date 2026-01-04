package com.ledgerx.core.evidence;

import java.time.Instant;
import java.util.Map;

public interface Evidence {

    String id();

    EvidenceType type();

    Instant observedAt();

    EvidenceSource source();

    double credibility(); // 0.0 - 1.0

    Map<String, Object> attributes();
}
