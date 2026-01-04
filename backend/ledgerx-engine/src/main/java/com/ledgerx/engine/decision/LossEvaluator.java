package com.ledgerx.engine.decision;

import com.ledgerx.core.belief.BeliefState;
import com.ledgerx.core.hypothesis.Hypothesis;
import com.ledgerx.core.loss.Loss;
import com.ledgerx.core.loss.LossModel;

import java.time.Duration;
import java.util.Map;

public class LossEvaluator {

    private final LossModel lossModel;
    private final Duration horizon;

    public LossEvaluator(LossModel lossModel, Duration horizon) {
        this.lossModel = lossModel;
        this.horizon = horizon;
    }

    public Loss expectedLoss(BeliefState beliefState) {

        double financial = 0;
        double regulatory = 0;
        double reputational = 0;
        boolean irreversible = false;

        for (Map.Entry<Hypothesis, Double> entry
                : beliefState.getProbabilities().entrySet()) {

            Hypothesis h = entry.getKey();
            double probability = entry.getValue();

            Loss l = lossModel.estimate(h, horizon);

            financial += probability * l.total();
            regulatory += probability * l.total();
            reputational += probability * l.total();

            if (l.irreversible()) {
                irreversible = true;
            }
        }

        return new Loss(financial, regulatory, reputational, irreversible);
    }
}
