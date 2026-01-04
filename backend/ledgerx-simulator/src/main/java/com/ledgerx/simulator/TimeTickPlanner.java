package com.ledgerx.simulator;

import com.ledgerx.engine.rules.AbsenceRule;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TimeTickPlanner {

    private final List<AbsenceRule> absenceRules;

    public TimeTickPlanner(List<AbsenceRule> absenceRules) {
        this.absenceRules = absenceRules;
    }

    /**
     * Returns time instants when TIME evidence must be injected
     */
    public Set<Instant> plannedTicks(Instant start) {

        return absenceRules.stream()
                .map(rule -> start.plus(rule.getAfter()))
                .collect(Collectors.toSet());
    }
}
