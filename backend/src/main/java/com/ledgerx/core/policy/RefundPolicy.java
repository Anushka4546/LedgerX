package com.ledgerx.core.policy;

import java.time.Duration;

public final class RefundPolicy {

    public final Duration settlementWaitWindow;
    public final double confidenceThreshold;
    public final boolean aggressiveRefunds;
    public final int monteCarloRuns;
    public final double waitHours;
    public final double chargebackWaitMultiplier;
    public final double maxAllowedLoss;

    public RefundPolicy(Duration settlementWaitWindow,
                        double confidenceThreshold,
                        boolean aggressiveRefunds,
                        int monteCarloRuns,
                        double waitHours,
                        double chargebackWaitMultiplier,
                        double maxAllowedLoss) {
        this.settlementWaitWindow = settlementWaitWindow;
        this.confidenceThreshold = confidenceThreshold;
        this.aggressiveRefunds = aggressiveRefunds;
        this.monteCarloRuns = monteCarloRuns;
        this.waitHours = waitHours;
        this.chargebackWaitMultiplier = chargebackWaitMultiplier;
        this.maxAllowedLoss = maxAllowedLoss;
    }
}
