# LedgerX Multi-Module Architecture

This is a multi-module Maven project organized by architectural layers.

## Module Structure

```
ledgerx/
│
├── pom.xml                     <-- parent POM
│
├── ledgerx-core/               <-- pure domain abstractions
│   └── src/
│       └── main/java/com/ledgerx/core/
│
├── ledgerx-engine/             <-- inference + reasoning
│   └── src/
│       └── main/java/com/ledgerx/engine/
│
├── ledgerx-config/             <-- YAML-driven intelligence
│   └── src/
│       └── main/resources/config/
│
├── ledgerx-simulator/          <-- mock Stripe/PayPal reality
│   └── src/
│       └── main/java/com/ledgerx/simulator/
│
└── ledgerx-api/                <-- Spring Boot app
    └── src/
        └── main/java/com/ledgerx/api/
```

## Module Dependencies

- **ledgerx-core**: No dependencies (pure domain model)
- **ledgerx-engine**: Depends on `ledgerx-core`
- **ledgerx-config**: Depends on `ledgerx-core`
- **ledgerx-simulator**: Depends on `ledgerx-core`, `ledgerx-engine`
- **ledgerx-api**: Depends on all other modules

## Building

```bash
# Build all modules
mvn clean install

# Build specific module
mvn clean install -pl ledgerx-api

# Run Spring Boot application
cd ledgerx-api
mvn spring-boot:run
```

## Module Responsibilities

### ledgerx-core
Pure domain abstractions - no external dependencies. Contains:
- Domain models (Evidence, RiskSnapshot, etc.)
- Value objects
- Enums (EvidenceType, EvidenceSource, etc.)

### ledgerx-engine
Inference and reasoning engine. Contains:
- Bayesian inference
- Decision engines
- Risk evaluation
- Monte Carlo simulation

### ledgerx-config
YAML-driven configuration. Contains:
- Rule definitions
- Policy configurations
- Intelligence parameters

### ledgerx-simulator
Mock payment gateway simulator. Contains:
- Stripe/PayPal behavior simulation
- Test data generators
- Scenario builders

### ledgerx-api
Spring Boot REST API. Contains:
- Controllers
- Services
- Configuration
- Application entry point

