package com.ledgerx.core.decision;

import com.ledgerx.core.domain.*;
import com.ledgerx.core.inference.*;
import com.ledgerx.core.policy.RefundPolicy;
import com.ledgerx.core.simulation.MonteCarloRiskEngine;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RefundDecisionEngine {

    private final DualRiskInference inference;
    private final RefundRiskEvaluator riskEvaluator;
    private final MonteCarloRiskEngine stressTester;

    public RefundDecisionEngine(Duration settlementWaitWindow) {
        this.inference = new DualRiskInference(
                new BayesianRefundInference(settlementWaitWindow),
                new ChargebackInference()
        );
        this.riskEvaluator = new RefundRiskEvaluator();
        this.stressTester = new MonteCarloRiskEngine();
    }

    public Decision evaluate(List<Evidence> evidence,
                             RefundPolicy policy,
                             LossModel lossModel,
                             Instant now) {

        List<String> reasons = new ArrayList<>();

        /* =====================================================
           HARD INVARIANTS
           ===================================================== */

        if (has(evidence, EvidenceType.SETTLEMENT_CONFIRMED)) {
            reasons.add("Bank settlement confirms refund already executed");
            return Decision.blocked(reasons);
        }

        if (has(evidence, EvidenceType.OPS_OVERRIDE_BLOCK)) {
            reasons.add("Ops explicitly blocked refund");
            return Decision.blocked(reasons);
        }

        if (has(evidence, EvidenceType.CHARGEBACK_FILED)) {
            reasons.add("Chargeback filed - requires human handling");
            return Decision.escalated(0.0, reasons);
        }

        if (has(evidence, EvidenceType.LEDGER_REFUND_RECORDED)
                && has(evidence, EvidenceType.SETTLEMENT_FAILED)) {

            reasons.add("Ledger/bank contradiction detected");
            return Decision.escalated(0.0, reasons);
        }

        /* =====================================================
           PROBABILISTIC INFERENCE
           ===================================================== */

        RiskSnapshot snapshot = inference.infer(evidence, now);

        double pRefundSafe = snapshot.pRefundSafe();
        double pChargeback = snapshot.pChargeback();

        reasons.add(String.format("P_refund_safe = %.4f", pRefundSafe));
        reasons.add(String.format("P_chargeback = %.4f", pChargeback));

        /* =====================================================
           EXPECTED LOSS (DETERMINISTIC)
           ===================================================== */

        RiskLossSnapshot loss =
                riskEvaluator.evaluate(
                        pRefundSafe,
                        pChargeback,
                        policy,
                        lossModel
                );

        double retryLoss = loss.retryLoss();
        double waitLoss  = loss.waitLoss();

        reasons.add(String.format("Expected RETRY loss = %.4f", retryLoss));
        reasons.add(String.format("Expected WAIT loss  = %.4f", waitLoss));

        /* =====================================================
           MONTE CARLO STRESS TEST (TAIL RISK)
           ===================================================== */

        RiskStressResult stress =
                stressTester.simulate(
                        snapshot,
                        policy,
                        lossModel,
                        policy.monteCarloRuns
                );

        reasons.add(String.format("P95 loss = %.4f", stress.p95Loss()));
        reasons.add(String.format("Worst-case loss = %.4f", stress.worstCaseLoss()));

        if (stress.worstCaseLoss() > policy.maxAllowedLoss) {
            reasons.add("Tail risk exceeds policy threshold");
            return Decision.escalated(pRefundSafe, reasons);
        }

        /* =====================================================
           DECISION
           ===================================================== */

        // Check for chargeback-related evidence that should force retry
        boolean hasChargebackAlert = has(evidence, EvidenceType.CHARGEBACK_ALERT)
                || has(evidence, EvidenceType.CUSTOMER_DISPUTE_OPENED);
        
        // Chargeback risk can force retry even with lower refund confidence
        // If chargeback alert exists, retry if it's not much worse than wait
        boolean chargebackForcesRetry = hasChargebackAlert 
                && (retryLoss < waitLoss || retryLoss < waitLoss * 1.2);

        if (retryLoss < waitLoss) {
            if (pRefundSafe >= policy.confidenceThreshold) {
                reasons.add("Retry minimizes expected loss");
                return Decision.retryNow(pRefundSafe, retryLoss, waitLoss, reasons);
            } else if (chargebackForcesRetry) {
                reasons.add("Chargeback risk forces retry despite refund uncertainty");
                return Decision.retryNow(pRefundSafe, retryLoss, waitLoss, reasons);
            }
        } else if (chargebackForcesRetry && retryLoss < waitLoss * 1.2) {
            // Chargeback alert: accept slightly higher retry loss to avoid chargeback escalation
            reasons.add("Chargeback alert: retry to prevent chargeback escalation");
            return Decision.retryNow(pRefundSafe, retryLoss, waitLoss, reasons);
        }

        if (waitLoss < lossModel.escalationCost) {
            reasons.add("Waiting cheaper than escalation");
            return Decision.wait(pRefundSafe, retryLoss, waitLoss, reasons);
        }

        reasons.add("Escalation minimizes long-term risk");
        return Decision.escalated(pRefundSafe, reasons);
    }

    private boolean has(List<Evidence> evidence, EvidenceType type) {
        return evidence.stream().anyMatch(e -> e.type == type);
    }
}
