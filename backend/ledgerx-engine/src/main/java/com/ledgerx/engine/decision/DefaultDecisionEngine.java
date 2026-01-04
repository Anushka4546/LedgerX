package com.ledgerx.engine.decision;

import com.ledgerx.core.action.Action;
import com.ledgerx.core.belief.BeliefState;
import com.ledgerx.core.decision.Decision;
import com.ledgerx.core.loss.Loss;
import com.ledgerx.engine.rules.ActionPolicy;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class DefaultDecisionEngine {

    private final ActionRegistry actionRegistry;
    private final List<ActionPolicy> policies;
    private final LossEvaluator lossEvaluator;

    public DefaultDecisionEngine(ActionRegistry actionRegistry,
                                 List<ActionPolicy> policies,
                                 LossEvaluator lossEvaluator) {
        this.actionRegistry = actionRegistry;
        this.policies = policies;
        this.lossEvaluator = lossEvaluator;
    }

    public Decision decide(BeliefState beliefState) {
        return decide(beliefState, false);
    }

    public Decision decide(BeliefState beliefState, boolean absenceTriggered) {

        Loss expectedLoss = lossEvaluator.expectedLoss(beliefState);

        for (ActionPolicy policy : policies) {

            if (!matchesProbability(policy, beliefState)) continue;
            if (!matchesLoss(policy, expectedLoss)) continue;
            if (!matchesAbsenceRequirement(policy, absenceTriggered)) continue;

            Action action = actionRegistry.get(policy.getActionId());
            if (action == null) continue;  // Skip if action not found

            return new Decision(
                    action,
                    beliefState,
                    expectedLoss,
                    buildJustification(policy, beliefState, expectedLoss),
                    Instant.now()
            );
        }

        // Fallback: try to find a default "WAIT" action if no policy matched
        Action fallbackAction = actionRegistry.get("WAIT");
        if (fallbackAction != null) {
            return new Decision(
                    fallbackAction,
                    beliefState,
                    expectedLoss,
                    "No policy matched, using default WAIT action",
                    Instant.now()
            );
        }

        throw new IllegalStateException(
                "No safe action policy matched for belief state and no WAIT action available");
    }

    private boolean matchesProbability(ActionPolicy policy,
                                       BeliefState beliefState) {

        if (policy.getWhenProbabilityExceeds() == null) return true;

        for (Map.Entry<String, Double> entry
                : policy.getWhenProbabilityExceeds().entrySet()) {

            double prob = beliefState.getProbabilities().entrySet().stream()
                    .filter(e -> e.getKey().id().equals(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(0.0);

            if (prob <= entry.getValue()) return false;
        }

        return true;
    }

    private boolean matchesLoss(ActionPolicy policy, Loss loss) {

        if (policy.getWhenExpectedLossExceeds() == null) return true;

        return loss.total() >= policy.getWhenExpectedLossExceeds();
    }

    private boolean matchesAbsenceRequirement(ActionPolicy policy, boolean absenceTriggered) {

        // If policy doesn't require absence, it always matches
        if (policy.getRequiresAbsence() == null || !policy.getRequiresAbsence()) {
            return true;
        }

        // If policy requires absence, it only matches if absence has triggered
        return absenceTriggered;
    }

    private String buildJustification(ActionPolicy policy,
                                      BeliefState beliefState,
                                      Loss loss) {

        return String.format(
                "Action '%s' selected due to belief probabilities %s " +
                "and expected loss %.2f",
                policy.getActionId(),
                beliefState.getProbabilities(),
                loss.total()
        );
    }
}
