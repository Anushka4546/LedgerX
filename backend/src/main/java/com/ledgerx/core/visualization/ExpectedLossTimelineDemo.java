package com.ledgerx.core.visualization;

import com.ledgerx.core.decision.*;
import com.ledgerx.core.domain.*;
import com.ledgerx.core.policy.RefundPolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Demonstrates how expected loss changes over time
 * and where decision flips from WAIT → RETRY.
 */
public class ExpectedLossTimelineDemo {

    public static void main(String[] args) {

        RefundDecisionEngine engine =
                new RefundDecisionEngine(Duration.ofHours(24));

        RefundPolicy policy =
                new RefundPolicy(
                        Duration.ofHours(24),
                        0.7,
                        false,
                        50_000,
                        4.0,
                        1.5,    // chargebackWaitMultiplier
                        1000.0  // maxAllowedLoss
                );

        LossModel loss =
                new LossModel(
                        100.0,   // double refund
                        5.0,     // delay cost per hour
                        200.0,   // escalation cost
                        50.0     // chargeback cost
                );

        Instant t0 = Instant.now().minus(Duration.ofHours(48));

        List<Evidence> evidence = List.of(
                new Evidence(
                        EvidenceSource.GATEWAY,
                        EvidenceType.REFUND_FAILED,
                        t0
                )
        );

        System.out.println("Hours | RetryLoss | WaitLoss | Action");
        System.out.println("----------------------------------------");

        for (int h = 0; h <= 48; h += 2) {

            Instant now = t0.plus(Duration.ofHours(h));

            Decision d =
                    engine.evaluate(evidence, policy, loss, now);

            System.out.printf(
                    "%5d | %9.2f | %8.2f | %s%n",
                    h,
                    d.expectedLossRetry,
                    d.expectedLossWait,
                    d.action
            );
        }
    }
}
