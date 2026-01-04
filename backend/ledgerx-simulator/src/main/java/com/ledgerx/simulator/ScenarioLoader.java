package com.ledgerx.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ledgerx.simulator.scenario.Scenario;
import com.ledgerx.simulator.scenario.ScenarioWrapper;

import java.io.InputStream;

public class ScenarioLoader {

    public static Scenario load(String fileName) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            InputStream is = ScenarioLoader.class
                    .getClassLoader()
                    .getResourceAsStream("scenarios/" + fileName);

            // YAML has "scenario:" as root, so use wrapper
            ScenarioWrapper wrapper = mapper.readValue(is, ScenarioWrapper.class);
            if (wrapper != null && wrapper.getScenario() != null) {
                return wrapper.getScenario();
            }
            
            throw new RuntimeException("Failed to load scenario: wrapper or scenario is null");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load scenario from " + fileName, e);
        }
    }
}
