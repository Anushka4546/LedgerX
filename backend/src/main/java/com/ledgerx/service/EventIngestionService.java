package com.ledgerx.service;

import com.ledgerx.model.Evidence;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class EventIngestionService {

    private final EvidenceService evidenceService;

    public EventIngestionService(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @KafkaListener(topics = "financial-events", groupId = "ledgerx")
    public void consume(Map<String, Object> event) {

        Evidence evidence = new Evidence(
                (String) event.get("transactionId"),
                (String) event.get("source"),
                (String) event.get("eventType"),
                event.toString(),
                Instant.parse((String) event.get("occurredAt"))
        );

        evidenceService.save(evidence);
    }
}

