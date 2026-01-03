package com.ledgerx.core.inference;

import com.ledgerx.core.domain.Evidence;
import com.ledgerx.core.domain.EvidenceType;

import java.time.Instant;
import java.util.List;

public class ChargebackInference {

    public double computeChargebackProbability(
            List<Evidence> evidence,
            Instant now
    ) {
        double logOdds = Math.log(0.05 / 0.95); // low prior

        for (Evidence e : evidence) {
            double llr = logLikelihood(e.type);
            double decay = TemporalDecay.decay(e, now);
            logOdds += llr * decay;
        }

        return sigmoid(logOdds);
    }

    private double logLikelihood(EvidenceType type) {
        return switch (type) {

            case CHARGEBACK_ALERT ->
                Math.log(0.6 / 0.4);

            case CUSTOMER_DISPUTE_OPENED ->
                Math.log(0.75 / 0.25);

            case CHARGEBACK_FILED ->
                Math.log(0.95 / 0.05);

            case CHARGEBACK_REVERSED ->
                Math.log(0.1 / 0.9);

            // Waiting too long increases chargeback risk
            case SETTLEMENT_NOT_OBSERVED ->
                Math.log(0.65 / 0.35);

            default -> 0.0;
        };
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
}

