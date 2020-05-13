package com.kajdreef.analyzer.visitor.Components;

import java.util.Map;
import java.util.Objects;
import java.util.HashMap;


public class Method {
    public final String methodDecl;
    public final String methodName;
    public final String className;
    public final String packageName;
    public final String filePath;
    public int lineStart;
    public int lineEnd;

    private Map<String, Object> properties;

    public Method(String methodDecl, String methodName, String className, String packageName, String filePath, int lineStart, int lineEnd) {
        this.methodDecl = methodDecl;
        this.methodName = methodName;
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.properties = new HashMap<>();
    }

    public void setLineRange(int start, int end) {
        this.lineStart = start;
        this.lineEnd = end;
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public Object getProperty(String key) {
        if (this.properties.containsKey(key)) {
            return this.properties.get(key);
        }
        return null;
    }

    @Override
    public boolean equals(Object other){
        if (this == other) {
            return true;
        }

        if (!(other instanceof Method)){
            return false;
        }

        Method otherMethod = (Method) other;

        return otherMethod.methodDecl.equals(this.methodDecl) &&
                otherMethod.methodName.equals(this.methodName)&&
                otherMethod.className.equals(this.className) &&
                otherMethod.packageName.equals(this.packageName) &&
                otherMethod.filePath.equals(this.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodDecl, methodName, className, packageName, filePath);
    }
}