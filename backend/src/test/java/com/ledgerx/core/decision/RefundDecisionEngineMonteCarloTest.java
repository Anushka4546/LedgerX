package com.ledgerx.core.decision;

import com.ledgerx.core.domain.*;
import com.ledgerx.core.policy.RefundPolicy;
import com.ledgerx.core.simulation.MonteCarloRiskEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Monte Carlo integration + loss-based decisions.
 * These tests validate statistical properties, not exact values.
 */
public class RefundDecisionEngineMonteCarloTest {

    private RefundDecisionEngine engine;
    private RefundPolicy policy;
    private LossModel lossModel;

    private Instant now;

    @BeforeEach
    void setup() {
        engine = new RefundDecisionEngine(Duration.ofHours(24));

        policy = new RefundPolicy(
                Duration.ofHours(24),
                0.7,            // confidence threshold
                false,
                50_000,         // Monte Carlo runs
                4.0,            // wait hours
                1.5,            // chargebackWaitMultiplier
                1000.0          // maxAllowedLoss
        );

        lossModel = new LossModel(
                100.0,          // double refund cost
                5.0,            // delay cost per hour
                200.0,          // escalation cost
                50.0            // chargeback cost
        );

        now = Instant.now();
    }

    /* ---------------------------------------------------
       MONTE CARLO CORE PROPERTIES
       --------------------------------------------------- */

    @Test
    void retryLossIncreasesWithProbability() {
        MonteCarloRiskEngine riskEngine = new MonteCarloRiskEngine();

        double lowRisk =
                riskEngine.expectedLossIfRetryNow(0.1, 50_000, lossModel);

        double highRisk =
                riskEngine.expectedLossIfRetryNow(0.9, 50_000, lossModel);

        assertTrue(highRisk > lowRisk,
                "Retry loss must increase with probability of prior execution");
    }

    @Test
    void retryLossApproximatesProbabilityTimesCost() {
        MonteCarloRiskEngine riskEngine = new MonteCarloRiskEngine();

        double p = 0.3;
        double expected =
                riskEngine.expectedLossIfRetryNow(p, 100_000, lossModel);

        double theoretical = p * lossModel.doubleRefundCost;

        assertTrue(Math.abs(expected - theoretical) < 3.0,
                "Monte Carlo loss should approximate p × cost");
    }

    @Test
    void waitLossIsDeterministic() {
        MonteCarloRiskEngine riskEngine = new MonteCarloRiskEngine();

        double waitLoss =
                riskEngine.expectedLossIfWait(4.0, lossModel);

        assertEquals(20.0, waitLoss);
    }

    /* ---------------------------------------------------
       DECISION BEHAVIOR TESTS
       --------------------------------------------------- */

    @Test
    void highConfidenceLowRiskLeadsToRetry() {

        List<Evidence> evidence = List.of(
                new Evidence(
                        EvidenceSource.GATEWAY,
                        EvidenceType.REFUND_FAILED,
                        now.minus(Duration.ofHours(30))
                )
        );

        Decision decision =
                engine.evaluate(evidence, policy, lossModel, now);

        assertEquals(RefundAction.RETRY_NOW, decision.action);
        assertEquals(RefundState.SAFE_TO_ISSUE, decision.state);
    }

    @Test
    void lowConfidenceHighRiskLeadsToWait() {

        List<Evidence> evidence = List.of(
                new Evidence(
                        EvidenceSource.GATEWAY,
                        EvidenceType.REFUND_SUCCESS,
                        now.minus(Duration.ofHours(2))
                )
        );

        Decision decision =
                engine.evaluate(evidence, policy, lossModel, now);

        assertEquals(RefundAction.WAIT, decision.action);
    }

    @Test
    void extremeRiskLeadsToEscalation() {

        List<Evidence> evidence = List.of(
                new Evidence(
                        EvidenceSource.LEDGER,
                        EvidenceType.LEDGER_REFUND_RECORDED,
                        now.minus(Duration.ofHours(1))
                ),
                new Evidence(
                        EvidenceSource.BANK,
                        EvidenceType.SETTLEMENT_FAILED,
                        now.minus(Duration.ofHours(1))
                )
        );

        Decision decision =
                engine.evaluate(evidence, policy, lossModel, now);

        assertEquals(RefundAction.ESCALATE, decision.action);
        assertEquals(RefundState.ESCALATED, decision.state);
    }
}
