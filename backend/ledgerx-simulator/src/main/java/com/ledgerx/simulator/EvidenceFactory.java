package com.ledgerx.simulator;

import com.ledgerx.core.evidence.*;
import com.ledgerx.simulator.scenario.EvidenceSpec;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class EvidenceFactory {

    public static Evidence from(EvidenceSpec spec, Instant observedAt) {

        return new Evidence() {
            @Override
            public String id() {
                return UUID.randomUUID().toString();
            }

            @Override
            public EvidenceType type() {
                return EvidenceType.valueOf(spec.getType());
            }

            @Override
            public Instant observedAt() {
                return observedAt;
            }

            @Override
            public EvidenceSource source() {
                try {
                    return EvidenceSource.valueOf(spec.getSource().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return EvidenceSource.OPS; // default fallback
                }
            }

            @Override
            public double credibility() {
                return spec.getCredibility();
            }

            @Override
            public Map<String, Object> attributes() {
                return spec.getAttributes();
            }
        };
    }
}
