package com.ledgerx.engine.rules;

import java.time.Duration;
import java.util.Map;

public class AbsenceRule {

    private String expectedEvidenceType;
    /**
     * Optional: specific attribute value to match (e.g., "name" -> "settlement")
     * If null, matches any evidence of the expected type
     */
    private Map<String, Object> expectedAttributes;
    private Duration after;
    /**
     * HypothesisId -> weight multiplier
     * weight > 1.0 strengthens belief when evidence is absent
     * Example: {"event_failed": 1.4} means absence increases confidence in failure
     */
    private Map<String, Double> hypothesisWeights;

    public String getExpectedEvidenceType() {
        return expectedEvidenceType;
    }

    public Map<String, Object> getExpectedAttributes() {
        return expectedAttributes;
    }

    public Duration getAfter() {
        return after;
    }

    public Map<String, Double> getHypothesisWeights() {
        return hypothesisWeights;
    }
}
