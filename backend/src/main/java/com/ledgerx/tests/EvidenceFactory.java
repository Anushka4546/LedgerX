package com.ledgerx.tests;

import com.ledgerx.core.domain.*;

import java.time.Instant;

public class EvidenceFactory {

    public static Evidence gatewaySuccess(Instant t) {
        return new Evidence(EvidenceSource.GATEWAY, EvidenceType.REFUND_SUCCESS, t);
    }

    public static Evidence gatewayFailed(Instant t) {
        return new Evidence(EvidenceSource.GATEWAY, EvidenceType.REFUND_FAILED, t);
    }

    public static Evidence bankSettled(Instant t) {
        return new Evidence(EvidenceSource.BANK, EvidenceType.SETTLEMENT_CONFIRMED, t);
    }

    public static Evidence bankFailed(Instant t) {
        return new Evidence(EvidenceSource.BANK, EvidenceType.SETTLEMENT_FAILED, t);
    }

    public static Evidence ledgerRecorded(Instant t) {
        return new Evidence(EvidenceSource.LEDGER, EvidenceType.LEDGER_REFUND_RECORDED, t);
    }

    public static Evidence opsAllow(Instant t) {
        return new Evidence(EvidenceSource.OPS, EvidenceType.OPS_OVERRIDE_ALLOW, t);
    }

    public static Evidence opsBlock(Instant t) {
        return new Evidence(EvidenceSource.OPS, EvidenceType.OPS_OVERRIDE_BLOCK, t);
    }
}
