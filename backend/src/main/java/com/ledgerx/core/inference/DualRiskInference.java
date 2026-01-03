package com.ledgerx.core.inference;

import com.ledgerx.core.domain.Evidence;
import com.ledgerx.core.domain.RiskSnapshot;

import java.time.Instant;
import java.util.List;

public class DualRiskInference {

    private final BayesianRefundInference refundInference;
    private final ChargebackInference chargebackInference;

    public DualRiskInference(
            BayesianRefundInference refundInference,
            ChargebackInference chargebackInference
    ) {
        this.refundInference = refundInference;
        this.chargebackInference = chargebackInference;
    }

    public RiskSnapshot infer(List<Evidence> evidence, Instant now) {
        return new RiskSnapshot(
                refundInference.computeSafeProbability(evidence, now),
                chargebackInference.computeChargebackProbability(evidence, now)
        );
    }
}
