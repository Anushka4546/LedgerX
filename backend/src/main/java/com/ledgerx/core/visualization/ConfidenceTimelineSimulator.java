package com.ledgerx.core.visualization;

import com.ledgerx.core.domain.*;
import com.ledgerx.core.inference.BayesianRefundInference;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulates how confidence evolves as time passes
 * and new evidence arrives.
 */
public class ConfidenceTimelineSimulator {

    private final BayesianRefundInference inference =
            new BayesianRefundInference(Duration.ofHours(24));

    public List<ConfidencePoint> simulate(
            List<Evidence> evidence,
            Instant startTime,
            Duration totalDuration,
            Duration step
    ) {

        List<ConfidencePoint> timeline = new ArrayList<>();

        Instant current = startTime;
        Instant end = startTime.plus(totalDuration);

        while (!current.isAfter(end)) {
            double confidence =
                    inference.computeSafeProbability(evidence, current);

            timeline.add(new ConfidencePoint(current, confidence));
            current = current.plus(step);
        }

        return timeline;
    }

    /* ---------------------------------------------
     * Value Object
     * --------------------------------------------- */
    public static class ConfidencePoint {
        public final Instant time;
        public final double confidence;

        public ConfidencePoint(Instant time, double confidence) {
            this.time = time;
            this.confidence = confidence;
        }
    }
}
