package com.ledgerx.engine.inference;

import com.ledgerx.core.belief.BeliefState;
import com.ledgerx.core.evidence.Evidence;
import com.ledgerx.core.hypothesis.Hypothesis;
import com.ledgerx.engine.decay.DecayPolicy;
import com.ledgerx.engine.rules.EvidenceRule;
import com.ledgerx.engine.rules.AbsenceRule;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultInferenceEngine implements InferenceEngine {

    private final Map<String, Hypothesis> hypothesisRegistry;
    private final List<EvidenceRule> evidenceRules;
    private final List<AbsenceRule> absenceRules;
    private final DecayPolicy decayPolicy;

    public DefaultInferenceEngine(
            Map<String, Hypothesis> hypothesisRegistry,
            List<EvidenceRule> evidenceRules,
            List<AbsenceRule> absenceRules,
            DecayPolicy decayPolicy) {

        this.hypothesisRegistry = hypothesisRegistry;
        this.evidenceRules = evidenceRules;
        this.absenceRules = absenceRules;
        this.decayPolicy = decayPolicy;
    }

    @Override
    public BeliefState update(BeliefState current, List<Evidence> evidenceList) {
        // Default: use only current evidence for absence detection
        return update(current, evidenceList, evidenceList);
    }

    /**
     * Update belief state with new evidence, using all observed evidence for absence detection
     */
    public BeliefState update(BeliefState current, List<Evidence> evidenceList, List<Evidence> allObservedEvidence) {

        Map<Hypothesis, Double> updated = new HashMap<>();

        // Use the latest evidence time, not system time
        Instant now = evidenceList.isEmpty() 
                ? Instant.now()
                : evidenceList.stream()
                    .map(Evidence::observedAt)
                    .max(Instant::compareTo)
                    .orElse(Instant.now());
        
        Duration elapsed = Duration.between(current.getAsOf(), now);

        // 1️⃣ Time decay
        current.getProbabilities().forEach((h, p) ->
                updated.put(h, decayPolicy.apply(p, elapsed)));

        // 2️⃣ Evidence influence
        applyEvidence(evidenceList, updated);

        // 3️⃣ Absence influence (use ALL observed evidence, not just current batch)
        applyAbsence(allObservedEvidence, updated, now);

        // 4️⃣ Normalize
        normalize(updated);

        return new BeliefState(now, updated);
    }

    /**
     * Check if any absence rules have triggered for the given evidence and time
     */
    public boolean hasAbsenceTriggered(List<Evidence> allObservedEvidence, Instant now) {
        AbsenceDetector detector = new AbsenceDetector(absenceRules);
        List<AbsenceRule> triggered = detector.detect(allObservedEvidence, now);
        return !triggered.isEmpty();
    }

    private void applyEvidence(List<Evidence> evidence,
                               Map<Hypothesis, Double> updated) {

        for (Evidence e : evidence) {
            for (EvidenceRule rule : evidenceRules) {

                if (!rule.getEvidenceType().equalsIgnoreCase(e.type().name())) {
                    continue;
                }

                // Check attribute match if specified
                if (rule.getExpectedAttributes() != null && !rule.getExpectedAttributes().isEmpty()) {
                    Map<String, Object> evidenceAttrs = e.attributes();
                    boolean matches = rule.getExpectedAttributes().entrySet().stream()
                            .allMatch(entry ->
                                evidenceAttrs.containsKey(entry.getKey()) &&
                                evidenceAttrs.get(entry.getKey()).equals(entry.getValue())
                            );
                    if (!matches) {
                        continue; // Skip this rule if attributes don't match
                    }
                }

                rule.getHypothesisWeights().forEach((id, weight) -> {
                    Hypothesis h = hypothesisRegistry.get(id);
                    if (h == null) return;
                    
                    // Only update if hypothesis is already in the map (from decay)
                    if (!updated.containsKey(h)) return;

                    // Multiplicative update: P(h) = P(h) * weight^credibility
                    double currentProb = updated.get(h);
                    if (currentProb <= 0.0) return;
                    
                    double adjustedWeight = Math.pow(weight, e.credibility());
                    double newProb = currentProb * adjustedWeight;
                    updated.put(h, newProb);
                });
            }
        }
    }

    private void applyAbsence(List<Evidence> evidence,
                              Map<Hypothesis, Double> updated,
                              Instant now) {

        AbsenceDetector detector = new AbsenceDetector(absenceRules);
        List<AbsenceRule> triggered = detector.detect(evidence, now);

        for (AbsenceRule rule : triggered) {
            rule.getHypothesisWeights().forEach((id, weight) -> {
                Hypothesis h = hypothesisRegistry.get(id);
                if (h == null) return;
                
                // Only update if hypothesis is already in the map (from decay)
                if (!updated.containsKey(h)) return;

                // Multiplicative update: P(h) = P(h) * weight
                double currentProb = updated.get(h);
                if (currentProb <= 0.0) return;
                
                double newProb = currentProb * weight;
                updated.put(h, newProb);
            });
        }
    }

    private void normalize(Map<Hypothesis, Double> probabilities) {
        double sum = probabilities.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (sum > 0.0) {
            probabilities.replaceAll((h, p) -> p / sum);
        }
    }
}
