package com.ledgerx.core.visualization;

import com.ledgerx.core.domain.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ConfidenceTimelineDemo {

    public static void main(String[] args) {

        Instant t0 = Instant.now().minus(Duration.ofHours(24));

        List<Evidence> evidence = List.of(
                new Evidence(
                        EvidenceSource.GATEWAY,
                        EvidenceType.REFUND_FAILED,
                        t0
                )
                // NOTE: no bank settlement yet
        );

        ConfidenceTimelineSimulator simulator =
                new ConfidenceTimelineSimulator();

        List<ConfidenceTimelineSimulator.ConfidencePoint> timeline =
                simulator.simulate(
                        evidence,
                        t0,
                        Duration.ofHours(48),
                        Duration.ofHours(2)
                );

        System.out.println("Time (hrs) | P_safe");
        System.out.println("-------------------");

        for (var point : timeline) {
            long hours =
                    Duration.between(t0, point.time).toHours();

            System.out.printf(
                    "%8d | %.4f%n",
                    hours,
                    point.confidence
            );
        }
    }
}
