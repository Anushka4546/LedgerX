# LedgerX  
**Financial Truth & Reconciliation Engine for Distributed Payment Systems**

LedgerX is a failure-first platform that determines the **true financial state of transactions** across systems that cannot be transactionally coordinated like payment gateways, banks, and internal ledgers.

> LedgerX does **not process payments**.  
> It explains **what actually happened to money** when systems disagree.
> POC(Refund Retry)

---

## 🚨 The Problem

In real fintech systems, money flows through **multiple independent systems**:

- Payment gateways (webhooks, retries, duplicates)
- Internal ledgers (database writes, service failures)
- Banks & settlement systems (T+1 / T+2 batch reports)
- Human operations (manual overrides, support actions)

These systems:
- Fail independently  
- Report conflicting states  
- Cannot be rolled back together  

This leads to:
- “Payment succeeded but ledger missing”
- “Refund processed twice due to retries”
- “Settlement completed but app shows pending”
- Manual Excel-based reconciliation
- Audit and compliance risk

**LedgerX exists to resolve this chaos.**

---

## 🧠 What LedgerX Does

LedgerX ingests **financial signals**, not money.

It:
- Correlates events across systems
- Detects contradictions
- Determines safe vs unsafe transaction states
- Produces **audit-grade explanations**
- Enables ops teams to resolve issues confidently

> Think of LedgerX as **observability + truth resolution for money**.

---

## 🧩 Core Use Case (MVP Scenario)

### Refund Mismatch

1. Payment gateway sends `REFUND_SUCCESS` webhook (twice)
2. Internal ledger write fails once
3. Bank settlement arrives after 24 hours
4. System states are contradictory
5. LedgerX:
   - Correlates all evidence
   - Detects unsafe refund
   - Freezes further action
   - Surfaces a full event timeline
   - Allows ops to finalize the outcome with evidence

No blind retries.  
No guessing.  
No silent financial corruption.

---

## 🏗️ High-Level Architecture


---

## 🔍 Key Concepts

### 1. Financial Signals (Not a Single Source of Truth)
LedgerX ingests:
- Webhooks (at-least-once, unreliable)
- Batch settlement reports (delayed, final)
- Ledger writes (strongly consistent but fallible)
- Human ops actions (authoritative but risky)

No signal is trusted blindly.

---

### 2. Correlation Engine
Matches events using:
- Transaction IDs (when available)
- Time windows
- Amount and currency
- Confidence scoring

Handles:
- Out-of-order delivery
- Missing identifiers
- Duplicate events

---

### 3. Truth Resolution Engine
Determines final transaction state using deterministic rules, for example:
- Bank settlement > gateway webhook
- Duplicate success without ledger entry = unsafe
- Human override requires evidence snapshot

Every decision is:
- Deterministic
- Justified
- Auditable

---

### 4. Evidence Ledger (Audit-Grade)
An append-only store that records:
- Raw incoming events
- Correlation results
- Truth decisions
- Ops actions and overrides

No updates.  
No deletes.  
Only evidence.

---

## 🖥️ UI

LedgerX includes a minimal but real **operations dashboard**.

### MVP UI Features
- Transaction search
- Event timeline visualization
- Conflict indicators (SAFE / UNSAFE)
- Final resolved state
- Manual “Resolve” action
- Evidence snapshot view

This UI mirrors **internal fintech ops tools**, not consumer apps.

---

## 🧪 Data Strategy

LedgerX uses **synthetic but behavior-accurate data**.

The system simulates:
- Duplicate webhooks
- Delayed settlements
- Partial ledger failures
- Conflicting signals

This mirrors real production failure modes used in fintech staging and chaos testing.

---

## 🛠️ Tech Stack

### Backend
- Java
- Spring Boot
- Apache Kafka (event ingestion)
- PostgreSQL (evidence ledger)
- Redis (idempotency + correlation cache)
- Docker

### Frontend
- React (TypeScript)
- REST APIs
- Timeline and table-based views

### Infrastructure (Local / Dev)
- Docker Compose
- Kafka + Zookeeper
- PostgreSQL
- Redis

---

## 📈 What This Project Teaches

### Technical
- Event correlation under uncertainty
- Distributed failure reasoning
- Immutable, audit-grade data modeling
- Decision engines (not simple workflows)
- Human-in-the-loop system design

### Business & Systems Thinking
- Ops and finance pain points
- Compliance and audit considerations
- Financial risk containment
- Why retries and ACID are insufficient for money flows

---

## 🛣️ Roadmap

### Phase 1 (MVP)
- Refund mismatch scenario
- Event ingestion and correlation
- Truth resolution logic
- Basic ops UI

### Phase 2
- Additional failure scenarios
- Rule engine enhancements
- Improved visualization

### Phase 3 (Optional)
- Metrics and alerts
- Multiple transaction types
- Role-based access control for ops actions

---

## 🧠 Philosophy

LedgerX is about **trust**, not throughput.

> Money systems don’t fail because code crashes.  
> They fail because no one can explain what happened.

LedgerX exists to fix that.
