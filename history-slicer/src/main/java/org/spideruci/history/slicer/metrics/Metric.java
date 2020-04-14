package org.spideruci.history.slicer.metrics;

import java.util.List;

public abstract class Metric {
    protected String METRIC_TYPE;

    public String getMetricType() {
        return METRIC_TYPE;
    }
    public abstract String compute(List<String> log);
}