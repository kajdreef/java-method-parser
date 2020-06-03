package com.kajdreef.analyzer.visitor;

import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.kajdreef.analyzer.metrics.MethodSignatures;
import com.kajdreef.analyzer.visitor.Components.Method;
import com.kajdreef.analyzer.visitor.Components.MethodVersion;

public class MethodSignatureVisitor extends AbstractMethodVisitor {

    public MethodSignatureVisitor() {
        super("METHOD_SIGNATURE_VISITOR");
    }

    @Override
    public void visit(MethodDeclaration method, MethodSignatures signatures) {
        String methodDecl = method.getDeclarationAsString(false, false, false);
        String methodName = method.getName().getIdentifier();

        int start_line = method.getRange().get().begin.line,
            end_line = method.getRange().get().end.line;

        Method methodInfo = signatures.get(methodDecl, methodName, className, packageName, filePath);
        
        List<String> annotations = method.getAnnotations().stream().map(annotation -> annotation.getNameAsString())
                .collect(Collectors.toList());

        MethodVersion version = new MethodVersion(start_line, end_line);
        version.setProperty("access_modifier", method.getAccessSpecifier().toString());
        version.setProperty("method_size", end_line - start_line + 1);
        version.setProperty("annotations", annotations);
        methodInfo.addVersion(version);
        signatures.add(methodInfo);

        super.visit(method, signatures);
    }
}