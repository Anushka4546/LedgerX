package com.ledgerx.simulator;

import com.ledgerx.core.belief.BeliefState;
import com.ledgerx.core.evidence.Evidence;
import com.ledgerx.core.evidence.TimeTickEvidence;
import com.ledgerx.core.hypothesis.Hypothesis;
import com.ledgerx.engine.decision.DefaultDecisionEngine;
import com.ledgerx.engine.inference.InferenceEngine;
import com.ledgerx.engine.rules.AbsenceRule;
import com.ledgerx.simulator.scenario.*;

import java.time.Instant;
import java.util.*;

public class SimulationRunner {

    private final InferenceEngine inferenceEngine;
    private final DefaultDecisionEngine decisionEngine;
    private final Map<String, Hypothesis> hypothesisRegistry;
    private final List<AbsenceRule> absenceRules;

    public SimulationRunner(InferenceEngine inferenceEngine,
                            DefaultDecisionEngine decisionEngine,
                            Map<String, Hypothesis> hypothesisRegistry,
                            List<AbsenceRule> absenceRules) {
        this.inferenceEngine = inferenceEngine;
        this.decisionEngine = decisionEngine;
        this.hypothesisRegistry = hypothesisRegistry;
        this.absenceRules = absenceRules;
    }

    public void run(Scenario scenario) {

    System.out.println("▶ Running scenario: " + scenario.getName());

    Map<Hypothesis, Double> priors = new HashMap<>();
    hypothesisRegistry.values()
            .forEach(h -> priors.put(h, 1.0 / hypothesisRegistry.size()));

    BeliefState belief = new BeliefState(Instant.now(), priors);
    List<Evidence> observed = new ArrayList<>();

    Instant start = belief.getAsOf();

    // 1️⃣ Prepare timeline events
    List<TimelineEvent> timeline = scenario.getTimeline();

    // 2️⃣ Prepare time ticks
    TimeTickPlanner planner = new TimeTickPlanner(absenceRules);
    Set<Instant> ticks = planner.plannedTicks(start);

    // 3️⃣ Merge evidence times + tick times
    TreeMap<Instant, List<Evidence>> schedule = new TreeMap<>();

    for (TimelineEvent event : timeline) {
        Instant t = start.plusSeconds(event.getAtMinutes() * 60);
        Evidence e = EvidenceFactory.from(event.getEvidence(), t);
        schedule.computeIfAbsent(t, k -> new ArrayList<>()).add(e);
    }

    for (Instant tick : ticks) {
        schedule.computeIfAbsent(tick, k -> new ArrayList<>())
                .add(new TimeTickEvidence(tick, Map.of("reason", "absence_check")));
    }

        // 4️⃣ Run simulation
        for (Map.Entry<Instant, List<Evidence>> entry : schedule.entrySet()) {

        Instant now = entry.getKey();
        List<Evidence> evidenceBatch = entry.getValue();

        observed.addAll(evidenceBatch);

        // Pass all observed evidence for absence detection
        boolean absenceTriggered = false;
        if (inferenceEngine instanceof com.ledgerx.engine.inference.DefaultInferenceEngine) {
            com.ledgerx.engine.inference.DefaultInferenceEngine defaultEngine = 
                    (com.ledgerx.engine.inference.DefaultInferenceEngine) inferenceEngine;
            belief = defaultEngine.update(belief, evidenceBatch, observed);
            absenceTriggered = defaultEngine.hasAbsenceTriggered(observed, now);
        } else {
            belief = inferenceEngine.update(belief, evidenceBatch);
        }

        System.out.println("\n⏱ Time @" +
                java.time.Duration.between(start, now).toMinutes() + " min");

        evidenceBatch.forEach(e ->
                System.out.println("Evidence: " + e.type() + " " + e.attributes()));

        System.out.println("Belief: " + belief.getProbabilities());

        System.out.println("Decision: "
                + decisionEngine.decide(belief, absenceTriggered).action().id());
    }
}

}
