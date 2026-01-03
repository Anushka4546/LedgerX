package com.ledgerx.core.decision;

import com.ledgerx.core.domain.LossModel;
import com.ledgerx.core.domain.RiskLossSnapshot;
import com.ledgerx.core.policy.RefundPolicy;

public class RefundRiskEvaluator {

    public RiskLossSnapshot evaluate(
            double pRefundSafe,
            double pChargeback,
            RefundPolicy policy,
            LossModel loss
    ) {
        double pRefundExecuted = 1.0 - pRefundSafe;

        double retryLoss =
                pRefundExecuted * loss.doubleRefundCost
              + pChargeback * loss.chargebackCost;

        double waitLoss =
                policy.waitHours * loss.delayCostPerHour
              + pChargeback * loss.chargebackCost
                * policy.chargebackWaitMultiplier;

        return new RiskLossSnapshot(
                retryLoss,
                waitLoss
        );
    }
}
