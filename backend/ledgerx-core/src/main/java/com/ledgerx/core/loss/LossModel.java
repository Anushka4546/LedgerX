package com.ledgerx.core.loss;

import com.ledgerx.core.hypothesis.Hypothesis;

import java.time.Duration;

public interface LossModel {
    Loss estimate(Hypothesis hypothesis, Duration horizon);
}

