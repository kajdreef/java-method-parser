package com.kajdreef.analyzer.visitor;

import java.util.List;
import java.util.stream.Collectors;


import com.github.javaparser.ast.body.MethodDeclaration;
import com.kajdreef.analyzer.metrics.MethodSignatures;
import com.kajdreef.analyzer.visitor.Components.Method;

public class MethodSignatureVisitor extends AbstractMethodVisitor {

    public MethodSignatureVisitor() {
        super("METHOD_SIGNATURE_VISITOR");
    }

    @Override
    public void visit(MethodDeclaration method, MethodSignatures signatures) {
        String methodDecl = method.getDeclarationAsString(false, false, false);

        int start_line = method.getRange().get().begin.line,
            end_line = method.getRange().get().end.line;

        Method methodInfo = signatures.get(methodDecl, className, packageName, filePath);
        methodInfo.setLineRange(start_line, end_line);

        List<String> annotations = method.getAnnotations().stream().map(annotation -> annotation.getNameAsString())
                .collect(Collectors.toList());

        methodInfo.addProperty("access_modifier", method.getAccessSpecifier().toString());
        methodInfo.addProperty("method_size", end_line - start_line + 1);
        methodInfo.addProperty("annotations", annotations);
        
        signatures.add(methodInfo);

        super.visit(method, signatures);
    }
}