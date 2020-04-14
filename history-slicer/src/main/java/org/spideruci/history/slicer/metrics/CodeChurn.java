package org.spideruci.history.slicer.metrics;

import java.util.List;

public class CodeChurn extends Metric {

    public CodeChurn() {
        this.METRIC_TYPE = "code_churn";
    }

    @Override
    public String compute(List<String> logDiffs) {
        int churn = 0;

        if (logDiffs.size() <= 1) {
            return Integer.toString(churn);
        }

        for (String diff : logDiffs) {
            churn += churnInDiff(diff);
        }

        churn -= churnInDiff(logDiffs.get(0));

        return Integer.toString(churn);
    }


    private int churnInDiff(String diff) {
        int added = 0;
        int deleted = 0;

        String[] lines = diff.split("\n");
        for (String line : lines) {
            if (line.startsWith("+") && !line.startsWith("+++")) {
                added += 1;
            } else if (line.startsWith("-") && !line.startsWith("---")) {
                deleted += 1;
            }
        }

        return added + deleted;
    }
}