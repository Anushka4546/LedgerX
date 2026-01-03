package com.ledgerx.core.domain;

public final class LossModel {

    public final double doubleRefundCost;
    public final double delayCostPerHour;
    public final double escalationCost;
    public final double chargebackCost;

    public LossModel(double doubleRefundCost,
                     double delayCostPerHour,
                     double escalationCost,
                     double chargebackCost) {
        this.doubleRefundCost = doubleRefundCost;
        this.delayCostPerHour = delayCostPerHour;
        this.escalationCost = escalationCost;
        this.chargebackCost = chargebackCost;
    }
}
