package com.ledgerx.simulator.scenario;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScenarioWrapper {
    
    @JsonProperty("scenario")
    private Scenario scenario;

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }
}

