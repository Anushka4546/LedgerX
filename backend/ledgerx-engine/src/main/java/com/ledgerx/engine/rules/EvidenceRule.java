package com.ledgerx.engine.rules;

import java.util.Map;

public class EvidenceRule {

    private String evidenceType;
    private String source;
    /**
     * Optional: specific attribute values to match (e.g., "name" -> "settlement")
     * If null, matches any evidence of the expected type
     */
    private Map<String, Object> expectedAttributes;
    /**
     * HypothesisId -> weight multiplier
     * weight > 1.0 strengthens belief, < 1.0 weakens belief
     * Example: {"event_delayed": 1.3, "event_failed": 0.7}
     */
    private Map<String, Double> hypothesisWeights;

    public String getEvidenceType() {
        return evidenceType;
    }

    public String getSource() {
        return source;
    }

    public Map<String, Object> getExpectedAttributes() {
        return expectedAttributes;
    }

    public Map<String, Double> getHypothesisWeights() {
        return hypothesisWeights;
    }
}
