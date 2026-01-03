package com.ledgerx.core.domain;

public enum RefundState {

    OBSERVED,
    WAITING_FOR_SETTLEMENT,

    SETTLED_CONFIRMED,
    SETTLEMENT_FAILED,

    SAFE_TO_ISSUE,
    ESCALATED,
    BLOCKED
}
