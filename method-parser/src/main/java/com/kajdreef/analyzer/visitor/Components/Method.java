package com.kajdreef.analyzer.visitor.Components;

import java.util.Objects;
import java.util.Optional;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Method {
    public final String methodDecl;
    public final String methodName;
    public final String className;
    public final String packageName;
    public final String filePath;
    
    public List<Map<String, String>> history;
    public List<MethodVersion> versions;

    public Method(String methodDecl, String methodName, String className, String packageName, String filePath) {
        this.methodDecl = methodDecl;
        this.methodName = methodName;
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
        this.history = new LinkedList<>();
        this.versions = new LinkedList<>();
    }

    public void addVersion(MethodVersion version) {
        this.versions.add(version);
    }

    public Optional<MethodVersion> getVersion(int versionIndex) {
        if (versionIndex < versions.size() && versionIndex >= 0) {
            return Optional.of(versions.get(versionIndex));
        }

        return Optional.empty();
    }

    public void setHistory(List<Map<String, String>> history) {
        this.history = history;
    }

    public int getNumberOfVersions() {
        return versions.size();
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

