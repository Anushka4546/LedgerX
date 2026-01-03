package com.ledgerx.core.domain;

public enum EvidenceType {

    REFUND_REQUESTED,
    REFUND_SUCCESS,
    REFUND_FAILED,

    SETTLEMENT_CONFIRMED,
    SETTLEMENT_FAILED,
    SETTLEMENT_NOT_OBSERVED,

    LEDGER_REFUND_RECORDED,
    LEDGER_WRITE_FAILED,

    OPS_OVERRIDE_ALLOW,
    OPS_OVERRIDE_BLOCK,

    CHARGEBACK_ALERT,          // network / bank warning
    CUSTOMER_DISPUTE_OPENED,   // app / support signal
    CHARGEBACK_FILED,          // authoritative
    CHARGEBACK_REVERSED        // merchant won dispute
}
