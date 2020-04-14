package org.spideruci.history.slicer.metrics;

import java.util.List;

public class TotalCommits extends Metric {

    public TotalCommits() {
        this.METRIC_TYPE = "total_commits";
    }

    @Override
    public String compute(List<String> logDiffs) {
        return Integer.toString(logDiffs.size());
    }
}