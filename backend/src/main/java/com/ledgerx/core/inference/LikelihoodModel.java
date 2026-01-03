package com.ledgerx.core.inference;

import com.ledgerx.core.domain.EvidenceType;

public final class LikelihoodModel {

    private LikelihoodModel() {}

    public static double logLikelihoodRatio(EvidenceType type) {
        return switch (type) {

            case SETTLEMENT_CONFIRMED ->
                Math.log(0.01 / 0.99);

            case SETTLEMENT_FAILED ->
                Math.log(0.95 / 0.05);
            
            case SETTLEMENT_NOT_OBSERVED ->
                Math.log(0.8 / 0.2);   

            case LEDGER_REFUND_RECORDED ->
                Math.log(0.05 / 0.95);

            case REFUND_SUCCESS ->
                Math.log(0.4 / 0.6);

            case REFUND_FAILED ->
                Math.log(0.6 / 0.4);

            case OPS_OVERRIDE_ALLOW ->
                Math.log(0.99 / 0.01);

            case OPS_OVERRIDE_BLOCK ->
                Math.log(0.01 / 0.99);

            default -> 0.0;
        };
    }
}
