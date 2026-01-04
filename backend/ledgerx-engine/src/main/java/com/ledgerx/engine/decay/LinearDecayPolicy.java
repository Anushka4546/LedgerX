package com.ledgerx.engine.decay;

import java.time.Duration;

public class LinearDecayPolicy implements DecayPolicy {

    private final Duration halfLife;

    public LinearDecayPolicy(Duration halfLife) {
        if (halfLife.isZero() || halfLife.isNegative()) {
            throw new IllegalArgumentException("Half-life must be positive");
        }
        this.halfLife = halfLife;
    }

    @Override
    public double apply(double currentProbability, Duration elapsed) {

        if (elapsed.isZero() || elapsed.isNegative()) {
            return currentProbability;
        }

        double decayRatio =
                Math.min(1.0, (double) elapsed.toMillis() / halfLife.toMillis());

        double decayed = currentProbability * (1.0 - decayRatio);

        return Math.max(0.0, decayed);
    }
}
