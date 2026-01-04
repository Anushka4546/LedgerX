package com.ledgerx.engine.rules;

import java.util.Map;

public class ActionPolicy {

    private String actionId;

    /**
     * HypothesisId -> probability threshold
     * Example: settlement_failed > 0.6
     */
    private Map<String, Double> whenProbabilityExceeds;

    /**
     * Expected loss threshold
     */
    private Double whenExpectedLossExceeds;

    /**
     * If true, this action requires that an absence rule has triggered
     * Used to guard escalation behind risk signals, not just probability
     */
    private Boolean requiresAbsence;

    public String getActionId() {
        return actionId;
    }

    public Map<String, Double> getWhenProbabilityExceeds() {
        return whenProbabilityExceeds;
    }

    public Double getWhenExpectedLossExceeds() {
        return whenExpectedLossExceeds;
    }

    public Boolean getRequiresAbsence() {
        return requiresAbsence;
    }
}
