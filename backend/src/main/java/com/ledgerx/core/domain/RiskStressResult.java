package com.ledgerx.core.domain;

public record RiskStressResult(
        double p95Loss,
        double worstCaseLoss
) {}

