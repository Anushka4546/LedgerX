package com.ledgerx.engine.decay;

import java.time.Duration;

public interface DecayPolicy {

    /**
     * Apply decay to an existing probability based on elapsed time.
     *
     * @param currentProbability current belief (0.0 - 1.0)
     * @param elapsed time since last belief update
     * @return decayed probability (0.0 - 1.0)
     */
    double apply(double currentProbability, Duration elapsed);
}
