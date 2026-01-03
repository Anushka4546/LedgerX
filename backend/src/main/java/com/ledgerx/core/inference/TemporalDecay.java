package com.ledgerx.core.inference;

import com.ledgerx.core.domain.Evidence;

import java.time.Duration;
import java.time.Instant;

public final class TemporalDecay {

    private static final double HALF_LIFE_HOURS = 24.0;

    private TemporalDecay() {}

    public static double decay(Evidence e, Instant now) {
        double hours = Duration.between(e.occurredAt, now).toHours();
        return Math.exp(-Math.log(2) * hours / HALF_LIFE_HOURS);
    }
}
