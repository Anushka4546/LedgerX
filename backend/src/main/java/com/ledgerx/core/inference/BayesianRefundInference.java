package com.ledgerx.core.inference;

import com.ledgerx.core.domain.Evidence;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class BayesianRefundInference {

    private final SettlementAbsenceDetector absenceDetector;

    public BayesianRefundInference(Duration settlementWindow) {
        this.absenceDetector =
            new SettlementAbsenceDetector(settlementWindow);
    }

    public double computeSafeProbability(List<Evidence> evidence,
                                         Instant now) {

        double[] logOdds = {0.0}; // prior = 0.5, using array for lambda

        for (Evidence e : evidence) {
            double llr = LikelihoodModel.logLikelihoodRatio(e.type);
            double decay = TemporalDecay.decay(e, now);
            logOdds[0] += llr * decay;
        }

        absenceDetector
            .detect(evidence, now)
            .ifPresent(absence -> {
                double llr =
                    LikelihoodModel.logLikelihoodRatio(absence.type);
                logOdds[0] += llr;
            });

        return 1.0 / (1.0 + Math.exp(-logOdds[0]));
    }
}
