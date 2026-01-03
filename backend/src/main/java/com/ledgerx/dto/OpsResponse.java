package com.ledgerx.dto;

import com.ledgerx.model.Evidence;
import java.util.List;

public class OpsResponse {
    private String transactionId;
    private String status;
    private List<Evidence> evidence;

    public OpsResponse() {}

    public OpsResponse(String transactionId, String status, List<Evidence> evidence) {
        this.transactionId = transactionId;
        this.status = status;
        this.evidence = evidence;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Evidence> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<Evidence> evidence) {
        this.evidence = evidence;
    }
}

