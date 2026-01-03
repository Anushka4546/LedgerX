package com.ledgerx.core.decision;

import com.ledgerx.core.domain.*;
import com.ledgerx.core.policy.RefundPolicy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Adversarial tests for RefundDecisionEngine.
 *
 * These tests intentionally avoid happy paths.
 * They validate non-negotiable safety invariants under hostile,
 * contradictory, delayed, and reordered evidence conditions.
 *
 * If any test in this suite fails, the engine is UNSAFE.
 */
public class RefundDecisionEngineAdversarialTest {

    private RefundDecisionEngine engine;
    private RefundPolicy conservativePolicy;
    private RefundPolicy aggressivePolicy;
    private LossModel lossModel;

    private Instant now;

    /* ---------------------------------------------------
     * SETUP
     * --------------------------------------------------- */

    @BeforeEach
    void setup() {
        engine = new RefundDecisionEngine(Duration.ofHours(24));

        conservativePolicy = new RefundPolicy(
                Duration.ofHours(24),
                0.95,          // high confidence requirement
                false,
                50_000,
                4.0,
                1.5,           // chargebackWaitMultiplier
                1000.0         // maxAllowedLoss
        );

        aggressivePolicy = new RefundPolicy(
                Duration.ofHours(1),
                0.6,
                true,
                50_000,
                2.0,
                1.5,           // chargebackWaitMultiplier
                2000.0         // maxAllowedLoss (higher for aggressive)
        );

        lossModel = new LossModel(
                100.0,   // double refund cost
                5.0,     // delay cost per hour
                200.0,   // escalation cost
                50.0     // chargeback cost
        );

        now = Instant.now();
    }

    /* ---------------------------------------------------
     * INVARIANT I1: Bank settlement ALWAYS blocks automation
     * --------------------------------------------------- */

    @Test
    void bankSettlementAlwaysBlocksRefund_evenWithAggressivePolicy() {

        List<Evidence> evidence = List.of(
                gatewaySuccess(now.minusSeconds(3_600)),
                bankSettled(now)
        );

        Decision decision =
                engine.evaluate(evidence, aggressivePolicy, lossModel, now);

        assertEquals(RefundState.BLOCKED, decision.state);
        assertEquals(RefundAction.ESCALATE, decision.action);
    }

    /* ---------------------------------------------------
     * INVARIANT I2: Evidence order must never affect outcome
     * --------------------------------------------------- */

    @Test
    void evidenceOrderDoesNotAffectBlockedDecision() {

        Evidence e1 = gatewaySuccess(now.minusSeconds(5_000));
        Evidence e2 = bankSettled(now);

        Decision d1 =
                engine.evaluate(List.of(e1, e2),
                        conservativePolicy, lossModel, now);

        Decision d2 =
                engine.evaluate(List.of(e2, e1),
                        conservativePolicy, lossModel, now);

        assertEquals(d1.state, d2.state);
        assertEquals(RefundState.BLOCKED, d1.state);
    }

    /* ---------------------------------------------------
     * INVARIANT I3: Duplicate weak signals cannot force retry
     * --------------------------------------------------- */

    @Test
    void duplicateGatewayEventsCannotForceUnsafeRetry() {

        List<Evidence> evidence = List.of(
                gatewaySuccess(now.minusSeconds(4_000)),
                gatewaySuccess(now.minusSeconds(4_000)),
                gatewaySuccess(now.minusSeconds(4_000))
        );

        Decision decision =
                engine.evaluate(evidence, conservativePolicy, lossModel, now);

       assertEquals(RefundState.WAITING_FOR_SETTLEMENT, decision.state);
       assertEquals(RefundAction.WAIT, decision.action);
    }

    /* ---------------------------------------------------
     * INVARIANT I4: Late authoritative truth dominates belief
     * --------------------------------------------------- */

    @Test
    void lateBankSettlementOverridesEarlierBelief() {

        List<Evidence> earlyEvidence = List.of(
                gatewayFailed(now.minusSeconds(10_000))
        );

        Decision before =
                engine.evaluate(earlyEvidence,
                        aggressivePolicy, lossModel, now);

        assertTrue(before.confidence > 0.5,
                "Early confidence should lean safe");

        List<Evidence> laterEvidence = List.of(
                gatewayFailed(now.minusSeconds(10_000)),
                bankSettled(now)
        );

        Decision after =
                engine.evaluate(laterEvidence,
                        aggressivePolicy, lossModel, now);

        assertEquals(RefundState.BLOCKED, after.state);
        assertEquals(RefundAction.ESCALATE, after.action);
    }

    /* ---------------------------------------------------
     * INVARIANT I5: Accounting contradictions must escalate
     * --------------------------------------------------- */

    @Test
    void ledgerBankContradictionForcesEscalation() {

        List<Evidence> evidence = List.of(
                gatewaySuccess(now.minusSeconds(3_000)),
                ledgerRecorded(now.minusSeconds(3_000)),
                bankFailed(now.minusSeconds(3_000))
        );

        Decision decision =
                engine.evaluate(evidence,
                        conservativePolicy, lossModel, now);

        assertEquals(RefundState.ESCALATED, decision.state);
        assertEquals(RefundAction.ESCALATE, decision.action);
    }

    /* ---------------------------------------------------
     * PROPERTY-BASED SAFETY TEST
     * --------------------------------------------------- */

    @Test
    void randomNoiseNeverOverridesSettlementTruth() {

        for (int i = 0; i < 10_000; i++) {

            List<Evidence> evidence = new ArrayList<>();
            evidence.add(bankSettled(now));

            int noise = (int) (Math.random() * 5);
            for (int j = 0; j < noise; j++) {
                evidence.add(
                        gatewaySuccess(now.minusSeconds(j * 100))
                );
            }

            Collections.shuffle(evidence);

            Decision decision =
                    engine.evaluate(evidence,
                            aggressivePolicy, lossModel, now);

            assertEquals(RefundState.BLOCKED, decision.state);
            assertEquals(RefundAction.ESCALATE, decision.action);
        }
    }

    /* ---------------------------------------------------
     * EVIDENCE FACTORY (INTENTIONAL & LOCAL)
     * --------------------------------------------------- */

    private Evidence gatewaySuccess(Instant t) {
        return new Evidence(
                EvidenceSource.GATEWAY,
                EvidenceType.REFUND_SUCCESS,
                t
        );
    }

    private Evidence gatewayFailed(Instant t) {
        return new Evidence(
                EvidenceSource.GATEWAY,
                EvidenceType.REFUND_FAILED,
                t
        );
    }

    private Evidence bankSettled(Instant t) {
        return new Evidence(
                EvidenceSource.BANK,
                EvidenceType.SETTLEMENT_CONFIRMED,
                t
        );
    }

    private Evidence bankFailed(Instant t) {
        return new Evidence(
                EvidenceSource.BANK,
                EvidenceType.SETTLEMENT_FAILED,
                t
        );
    }

    private Evidence ledgerRecorded(Instant t) {
        return new Evidence(
                EvidenceSource.LEDGER,
                EvidenceType.LEDGER_REFUND_RECORDED,
                t
        );
    }
}
