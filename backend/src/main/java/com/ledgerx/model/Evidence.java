package com.ledgerx.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evidence")
public class Evidence {

    @Id
    @GeneratedValue
    private UUID id;

    private String transactionId;

    private String source; 
    // GATEWAY | LEDGER | BANK | OPS

    private String eventType;
    // REFUND_SUCCESS, SETTLEMENT_CONFIRMED, LEDGER_WRITE_FAILED, OPS_OVERRIDE

    @Column(columnDefinition = "TEXT")
    private String payload; // raw JSON

    private Instant occurredAt;
    private Instant ingestedAt;

    protected Evidence() {}

    public Evidence(String transactionId,
                    String source,
                    String eventType,
                    String payload,
                    Instant occurredAt) {
        this.transactionId = transactionId;
        this.source = source;
        this.eventType = eventType;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.ingestedAt = Instant.now();
    }

    // Getters only — immutable
    public UUID getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getSource() {
        return source;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }
}

