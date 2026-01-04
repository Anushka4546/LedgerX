package com.ledgerx.engine.inference;

import com.ledgerx.core.evidence.Evidence;
import com.ledgerx.engine.rules.AbsenceRule;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AbsenceDetector {

    private final List<AbsenceRule> absenceRules;

    public AbsenceDetector(List<AbsenceRule> absenceRules) {
        this.absenceRules = absenceRules;
    }

    public List<AbsenceRule> detect(
            List<Evidence> observedEvidence,
            Instant now) {

        List<AbsenceRule> triggered = new ArrayList<>();

        for (AbsenceRule rule : absenceRules) {

            boolean evidenceSeen = observedEvidence.stream()
                    .anyMatch(e -> {
                        // Check type match
                        if (!e.type().name().equalsIgnoreCase(rule.getExpectedEvidenceType())) {
                            return false;
                        }
                        
                        // Check attribute match if specified
                        if (rule.getExpectedAttributes() != null && !rule.getExpectedAttributes().isEmpty()) {
                            Map<String, Object> evidenceAttrs = e.attributes();
                            boolean matches = rule.getExpectedAttributes().entrySet().stream()
                                    .allMatch(entry -> 
                                        evidenceAttrs.containsKey(entry.getKey()) &&
                                        evidenceAttrs.get(entry.getKey()).equals(entry.getValue())
                                    );
                            return matches;
                        }
                        
                        return true; // Type matches and no specific attributes required
                    });

            if (evidenceSeen) {
                continue;
            }

            // Calculate elapsed from the earliest evidence (scenario start)
            // This ensures absence rules trigger based on absolute time from start
            Instant referenceTime = observedEvidence.stream()
                    .map(Evidence::observedAt)
                    .min(Instant::compareTo)
                    .orElse(now);
            
            // If 'now' appears to be system time (very close to reference), use latest evidence time instead
            Instant effectiveNow = now;
            Duration timeSinceReference = Duration.between(referenceTime, now);
            if (timeSinceReference.toMinutes() < 1 && !observedEvidence.isEmpty()) {
                Instant latestEvidenceTime = observedEvidence.stream()
                        .map(Evidence::observedAt)
                        .max(Instant::compareTo)
                        .orElse(now);
                effectiveNow = latestEvidenceTime;
            }

            Duration elapsed = Duration.between(referenceTime, effectiveNow);

            if (elapsed.compareTo(rule.getAfter()) >= 0) {
                triggered.add(rule);
            }
        }

        return triggered;
    }
}
