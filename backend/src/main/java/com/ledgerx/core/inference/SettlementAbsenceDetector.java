package com.ledgerx.core.inference;

import com.ledgerx.core.domain.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class SettlementAbsenceDetector {

    private final Duration expectedSettlementWindow;

    public SettlementAbsenceDetector(Duration expectedSettlementWindow) {
        this.expectedSettlementWindow = expectedSettlementWindow;
    }

    public Optional<Evidence> detect(
            Iterable<Evidence> existingEvidence,
            Instant now
    ) {
        boolean settlementSeen = false;
        Instant earliestEvent = null;

        for (Evidence e : existingEvidence) {
            if (e.type == EvidenceType.SETTLEMENT_CONFIRMED ||
                e.type == EvidenceType.SETTLEMENT_FAILED) {
                settlementSeen = true;
                break;
            }
            if (earliestEvent == null ||
                e.occurredAt.isBefore(earliestEvent)) {
                earliestEvent = e.occurredAt;
            }
        }

        if (settlementSeen || earliestEvent == null) {
            return Optional.empty();
        }

        Duration elapsed = Duration.between(earliestEvent, now);

        if (elapsed.compareTo(expectedSettlementWindow) >= 0) {
            return Optional.of(
                new Evidence(
                    EvidenceSource.BANK,
                    EvidenceType.SETTLEMENT_NOT_OBSERVED,
                    now
                )
            );
        }

        return Optional.empty();
    }
}
