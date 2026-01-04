package com.ledgerx.simulator;

import com.ledgerx.core.hypothesis.Hypothesis;
import com.ledgerx.engine.decay.LinearDecayPolicy;
import com.ledgerx.engine.decision.*;
import com.ledgerx.engine.inference.*;
import com.ledgerx.engine.rules.*;
import com.ledgerx.simulator.scenario.Scenario;

import java.time.Duration;
import java.util.*;

public class SimulationApp {

    public static void main(String[] args) {

        // 1️⃣ Hypotheses registry
        Map<String, Hypothesis> hypotheses = Map.of(
                "event_delayed", simpleHypothesis("event_delayed"),
                "event_failed", simpleHypothesis("event_failed")
        );

        // 2️⃣ Evidence rules (multiplicative weights)
        // weight > 1.0 strengthens belief, < 1.0 weakens belief
        
        // General event rule (authorization, capture, etc.)
        EvidenceRule eventRule = new EvidenceRule();
        set(eventRule, "EVENT", null, Map.of(
                "event_delayed", 1.6,  // Strengthens belief in delay
                "event_failed", 0.6   // Weakens belief in failure
        ));
        
        // Settlement rule: terminal evidence that strongly suppresses failure
        EvidenceRule settlementRule = new EvidenceRule();
        set(settlementRule, "EVENT", Map.of("name", "settlement"), Map.of(
                "event_failed", 0.1,  // Strongly suppress failure (terminal evidence)
                "event_delayed", 1.2  // Boost delay (settlement confirms it was just delayed)
        ));

        // 3️⃣ Absence rules (multiplicative weights)
        // Absence past SLA is risk-dominant: boost failure, suppress delay optimism
        AbsenceRule absenceRule = new AbsenceRule();
        setAbsence(absenceRule, "EVENT", Map.of("name", "settlement"), Duration.ofMinutes(90), Map.of(
                "event_failed", 4.0,  // Strong risk signal: absence significantly increases failure probability
                "event_delayed", 0.4   // Suppress delay optimism when evidence is absent past SLA
        ));

        // 4️⃣ Inference engine
        InferenceEngine inferenceEngine =
                new DefaultInferenceEngine(
                        hypotheses,
                        List.of(eventRule, settlementRule),  // Include both rules
                        List.of(absenceRule),
                        new LinearDecayPolicy(Duration.ofMinutes(120))
                );

        // 5️⃣ Actions
        ActionRegistry actionRegistry = new ActionRegistry(Map.of(
                "WAIT", simpleAction("WAIT", true),
                "ESCALATE", simpleAction("ESCALATE", false)
        ));

        // 6️⃣ Loss model
        LossEvaluator lossEvaluator =
                new LossEvaluator(
                        (h, d) -> h.id().equals("event_failed")
                                ? new com.ledgerx.core.loss.Loss(1000, 0, 0, true)
                                : new com.ledgerx.core.loss.Loss(10, 0, 0, false),
                        Duration.ofHours(2)
                );

        // 7️⃣ Decision engine
        DefaultDecisionEngine decisionEngine =
                new DefaultDecisionEngine(
                        actionRegistry,
                        List.of(
                                actionPolicy("ESCALATE", "event_failed", 0.25, true),  // Requires absence trigger (risk signal)
                                defaultActionPolicy("WAIT")  // Fallback: always matches
                        ),
                        lossEvaluator
                );

        // 8️⃣ Load scenario manually (for now)
        Scenario scenario = ScenarioLoader.load("delayed-settlement.yaml");

        // 9️⃣ Run simulation
        new SimulationRunner(
                inferenceEngine,
                decisionEngine,
                hypotheses,
                List.of(absenceRule)
        ).run(scenario);
    }

    // ---------- helpers ----------

    private static Hypothesis simpleHypothesis(String id) {
        return new Hypothesis() {
            public String id() { return id; }
            public String description() { return id; }
            public Set<String> tags() { return Set.of(); }
        };
    }

    private static com.ledgerx.core.action.Action simpleAction(
            String id, boolean reversible) {
        return new com.ledgerx.core.action.Action() {
            public String id() { return id; }
            public String description() { return "Action: " + id; }
        };
    }

    private static ActionPolicy actionPolicy(
            String actionId, String hypothesisId, double threshold) {
        return actionPolicy(actionId, hypothesisId, threshold, false);
    }

    private static ActionPolicy actionPolicy(
            String actionId, String hypothesisId, double threshold, boolean requiresAbsence) {
        ActionPolicy p = new ActionPolicy();
        try {
            var f1 = ActionPolicy.class.getDeclaredField("actionId");
            var f2 = ActionPolicy.class.getDeclaredField("whenProbabilityExceeds");
            var f3 = ActionPolicy.class.getDeclaredField("requiresAbsence");
            f1.setAccessible(true);
            f2.setAccessible(true);
            f3.setAccessible(true);
            f1.set(p, actionId);
            f2.set(p, Map.of(hypothesisId, threshold));
            f3.set(p, requiresAbsence);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return p;
    }

    private static ActionPolicy defaultActionPolicy(String actionId) {
        // Default policy with no constraints - always matches as fallback
        ActionPolicy p = new ActionPolicy();
        try {
            var f1 = ActionPolicy.class.getDeclaredField("actionId");
            var f2 = ActionPolicy.class.getDeclaredField("whenProbabilityExceeds");
            f1.setAccessible(true);
            f2.setAccessible(true);
            f1.set(p, actionId);
            f2.set(p, null);  // No probability constraint
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return p;
    }

    private static void set(EvidenceRule rule, String type, Map<String, Object> expectedAttributes, Map<String, Double> weights) {
        try {
            var f1 = EvidenceRule.class.getDeclaredField("evidenceType");
            var f2 = EvidenceRule.class.getDeclaredField("expectedAttributes");
            var f3 = EvidenceRule.class.getDeclaredField("hypothesisWeights");
            f1.setAccessible(true);
            f2.setAccessible(true);
            f3.setAccessible(true);
            f1.set(rule, type);
            f2.set(rule, expectedAttributes);
            f3.set(rule, weights);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setAbsence(
            AbsenceRule rule, String type, Map<String, Object> expectedAttributes, 
            Duration after, Map<String, Double> weights) {
        try {
            var f1 = AbsenceRule.class.getDeclaredField("expectedEvidenceType");
            var f2 = AbsenceRule.class.getDeclaredField("expectedAttributes");
            var f3 = AbsenceRule.class.getDeclaredField("after");
            var f4 = AbsenceRule.class.getDeclaredField("hypothesisWeights");
            f1.setAccessible(true);
            f2.setAccessible(true);
            f3.setAccessible(true);
            f4.setAccessible(true);
            f1.set(rule, type);
            f2.set(rule, expectedAttributes);
            f3.set(rule, after);
            f4.set(rule, weights);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
