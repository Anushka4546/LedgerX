package com.ledgerx.simulator.scenario;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimelineEvent {

    @JsonProperty("at_minutes")
    private long atMinutes;
    private EvidenceSpec evidence;

    public long getAtMinutes() {
        return atMinutes;
    }

    public void setAtMinutes(long atMinutes) {
        this.atMinutes = atMinutes;
    }

    public EvidenceSpec getEvidence() {
        return evidence;
    }

    public void setEvidence(EvidenceSpec evidence) {
        this.evidence = evidence;
    }
}
