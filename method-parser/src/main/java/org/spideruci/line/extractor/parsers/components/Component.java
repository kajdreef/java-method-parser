package org.spideruci.line.extractor.parsers.components;

import java.util.HashMap;
import java.util.Map;

public abstract class Component {
    public final Map<String, String> metrics;
    
    public Component(){
        this.metrics = new HashMap<>();
    }

    abstract public String asString();

    public void addMetricResult(String key, String value) {
        if (this.metrics.containsKey(key)) {
            return;
        }
        this.metrics.put(key, value);
    }

    public void addMetricResult(String key, int value) {
        this.addMetricResult(key, Integer.toString(value));
    }

    public Map<String, String> getMetrics() {
        return this.metrics;
    }
}