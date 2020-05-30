package com.kajdreef.analyzer.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.kajdreef.analyzer.visitor.Components.Method;

public class MethodSignatures {
    Map<Integer, Method> methodList;

    public MethodSignatures(){
        methodList = new HashMap<>();
    }

    public void add(Method m) {
        methodList.put(m.hashCode(), m);
    }

    public Method get(String methodDecl, String methodName, String className, String packageName, String filePath) {
        int key = Objects.hash(methodDecl, methodName, className, packageName, filePath);

        if (methodList.containsKey(key)) {
            return methodList.get(key);
        }
        
        return new Method(methodDecl, methodName, className, packageName, filePath);
    }

    public Map<Integer, Method> getMap() {
        return methodList;
    }
}