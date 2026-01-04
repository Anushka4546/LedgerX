package com.ledgerx.core.belief;

import com.ledgerx.core.hypothesis.Hypothesis;

import java.time.Instant;
import java.util.Map;

public class BeliefState {

    private final Instant asOf;
    private final Map<Hypothesis, Double> probabilities;

    public BeliefState(Instant asOf, Map<Hypothesis, Double> probabilities) {
        this.asOf = asOf;
        this.probabilities = Map.copyOf(probabilities);
    }

    public Instant getAsOf() {
        return asOf;
    }

    public Map<Hypothesis, Double> getProbabilities() {
        return probabilities;
    }
}
