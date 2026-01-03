package com.ledgerx.core.domain;

public record RiskLossSnapshot(
        double retryLoss,
        double waitLoss
) {}

