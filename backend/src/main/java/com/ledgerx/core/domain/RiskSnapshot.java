package com.ledgerx.core.domain;

public record RiskSnapshot(
        double pRefundSafe,
        double pChargeback
) {}
