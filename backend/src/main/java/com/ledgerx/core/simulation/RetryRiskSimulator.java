package com.ledgerx.core.simulation;

public class RetryRiskSimulator {

    public double simulateDoubleRefundRisk(double pAlreadyExecuted,
                                           int simulations) {

        int doubleRefunds = 0;

        for (int i = 0; i < simulations; i++) {
            boolean alreadyExecuted =
                    Math.random() < pAlreadyExecuted;

            if (alreadyExecuted) {
                doubleRefunds++;
            }
        }

        return (double) doubleRefunds / simulations;
    }
}
