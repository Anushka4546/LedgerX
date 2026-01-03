package com.ledgerx.core.decision;

import com.ledgerx.core.domain.*;
import com.ledgerx.core.policy.RefundPolicy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Adversarial tests focused on chargeback risk.
 *
 * These tests prove that the engine correctly trades off
 * refund risk vs chargeback risk under hostile conditions.
 */
public class RefundDecisionEngineChargebackAdversarialTest {

    private RefundDecisionEngine engine;
    private RefundPolicy policy;
    private LossModel loss;

    private Instant now;

    @BeforeEach
    void setup() {
        engine = new RefundDecisionEngine(Duration.ofHours(24));

        policy = new RefundPolicy(
                Duration.ofHours(24),
                0.7,
                false,
                50_000,
                4.0,
                1.5,    // chargebackWaitMultiplier
                1000.0  // maxAllowedLoss
        );

        loss = new LossModel(
                100.0,   // double refund cost
                5.0,     // delay cost per hour
                200.0,   // escalation cost
                500.0    // chargeback cost (very expensive)
        );

        now = Instant.now();
    }

    /* ---------------------------------------------------
       TEST 1: High chargeback risk forces early refund
       --------------------------------------------------- */

    @Test
    void highChargebackRiskForcesRetryEvenWithRefundUncertainty() {

        List<Evidence> evidence = List.of(
                new Evidence(
                        EvidenceSource.BANK,
                        EvidenceType.CHARGEBACK_ALERT,
                        now.minus(Duration.ofHours(1))
                ),
                new Evidence(
                        EvidenceSource.GATEWAY,
                        EvidenceType.REFUND_FAILED,
                        now.minus(Duration.ofHours(20))
                )
        );

        Decision decision =
                engine.evaluate(evidence, policy, loss, now);

        assertEquals(RefundAction.RETRY_NOW, decision.action,
                "High chargeback risk should dominate refund uncertainty");

        assertEquals(RefundState.SAFE_TO_ISSUE, decision.state);
    }

    /* ---------------------------------------------------
       TEST 2: Filed chargeback blocks automation
       --------------------------------------------------- */

    @Test
    void filedChargebackAlwaysEscalates() {

        List<Evidence> evidence = List.of(
                new Evidence(
                        EvidenceSource.BANK,
                        EvidenceType.CHARGEBACK_FILED,
                        now
                )
        );

        Decision decision =
                engine.evaluate(evidence, policy, loss, now);

        assertEquals(RefundAction.ESCALATE, decision.action,
                "Filed chargeback requires human handling");

        assertEquals(RefundState.ESCALATED, decision.state);
    }

    /* ---------------------------------------------------
       TEST 3: Waiting increases total expected loss
       --------------------------------------------------- */

    @Test
    void waitingBecomesMoreExpensiveThanRetryDueToChargebackRisk() {

        List<Evidence> evidence = List.of(
                new Evidence(
                        EvidenceSource.GATEWAY,
                        EvidenceType.REFUND_FAILED,
                        now.minus(Duration.ofHours(30))
                ),
                new Evidence(
                        EvidenceSource.BANK,
                        EvidenceType.CUSTOMER_DISPUTE_OPENED,
                        now.minus(Duration.ofHours(2))
                )
        );

        Decision decision =
                engine.evaluate(evidence, policy, loss, now);

        assertEquals(RefundAction.RETRY_NOW, decision.action,
                "Engine should retry to avoid escalating chargeback risk");
    }

    /* ---------------------------------------------------
       TEST 4: Low chargeback risk prefers waiting
       --------------------------------------------------- */

    @Test
    void lowChargebackRiskPrefersWaitingOverUnsafeRetry() {

        List<Evidence> evidence = List.of(
                new Evidence(
                        EvidenceSource.GATEWAY,
                        EvidenceType.REFUND_SUCCESS,
                        now.minus(Duration.ofHours(2))
                )
        );

        Decision decision =
                engine.evaluate(evidence, policy, loss, now);

        assertEquals(RefundAction.WAIT, decision.action,
                "Low chargeback risk should not justify unsafe retry");
    }
}

