package com.kajdreef.analyzer.visitor.Components;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MethodVersion {
    public final int lineStart;
    public final int lineEnd;
    public String commitId;
    public String commitDate;
    public Map<String, Object> properties;

    public MethodVersion(final int lineStart, final int lineEnd, String commitId, String commitDate, Map<String, Object> properties) {
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.commitId = commitId;
        this.commitDate = commitDate;
        this.properties = properties;
    }

    public MethodVersion(final int lineStart, final int lineEnd) {
        this(lineStart, lineEnd, null, null, new HashMap<>());
    }

    public void setCommitInfo(String commitId, String commitDate) {
        this.commitId = commitId;
        this.commitDate = commitDate;
    }

    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public Optional<Object> getProperty(String key) {
        if (this.properties.containsKey(key)) {
            return Optional.of(this.properties.get(key));
        }

        return Optional.empty();
    }
}