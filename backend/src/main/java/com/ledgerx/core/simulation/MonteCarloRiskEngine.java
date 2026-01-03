package com.ledgerx.core.simulation;

import com.ledgerx.core.domain.*;
import com.ledgerx.core.policy.RefundPolicy;

public class MonteCarloRiskEngine {

    /**
     * @param pAlreadyExecuted probability refund already happened elsewhere
     * @param simulations number of runs
     * @param lossModel business loss parameters
     */
    public double expectedLossIfRetryNow(
            double pAlreadyExecuted,
            int simulations,
            LossModel lossModel) {

        double totalLoss = 0.0;

        for (int i = 0; i < simulations; i++) {
            boolean alreadyExecuted = Math.random() < pAlreadyExecuted;

            if (alreadyExecuted) {
                // double refund
                totalLoss += lossModel.doubleRefundCost;
            } else {
                // correct refund
                totalLoss += 0.0;
            }
        }

        return totalLoss / simulations;
    }

    public double expectedLossIfWait(
            double hoursToWait,
            LossModel lossModel) {

        return hoursToWait * lossModel.delayCostPerHour;
    }

    public RiskStressResult simulate(
            RiskSnapshot snapshot,
            RefundPolicy policy,
            LossModel lossModel,
            int runs) {

        double[] losses = new double[runs];
        double pRefundExecuted = 1.0 - snapshot.pRefundSafe();

        for (int i = 0; i < runs; i++) {
            boolean refundExecuted = Math.random() < pRefundExecuted;
            boolean chargeback = Math.random() < snapshot.pChargeback();

            double loss = 0.0;
            if (refundExecuted) {
                loss += lossModel.doubleRefundCost;
            }
            if (chargeback) {
                loss += lossModel.chargebackCost;
            }
            losses[i] = loss;
        }

        java.util.Arrays.sort(losses);
        double p95Loss = losses[(int) (runs * 0.95)];
        double worstCaseLoss = losses[runs - 1];

        return new RiskStressResult(p95Loss, worstCaseLoss);
    }
}
