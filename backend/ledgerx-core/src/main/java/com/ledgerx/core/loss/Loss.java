package com.ledgerx.core.loss;

public record Loss(
        double financial,
        double regulatory,
        double reputational,
        boolean irreversible
) {
    public double total() {
        return financial + regulatory + reputational;
    }
}

