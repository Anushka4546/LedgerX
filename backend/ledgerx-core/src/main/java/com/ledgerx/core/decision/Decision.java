package com.ledgerx.core.decision;

import com.ledgerx.core.action.Action;
import com.ledgerx.core.belief.BeliefState;
import com.ledgerx.core.loss.Loss;

import java.time.Instant;

public record Decision(
        Action action,
        BeliefState beliefState,
        Loss expectedLoss,
        String justification,
        Instant decidedAt
) {}

