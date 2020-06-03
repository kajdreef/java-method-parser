package com.kajdreef.analyzer.visitor;

import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;

import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;

import com.kajdreef.analyzer.metrics.*;
import com.kajdreef.analyzer.visitor.Components.Method;
import com.kajdreef.analyzer.visitor.Components.MethodVersion;

public class MethodCyclomaticComplexityVisitor extends AbstractMethodVisitor {

    private final String CYCLOMATIC_ID = "cyclomatic_complexity";

    public MethodCyclomaticComplexityVisitor() {
        super("CC_VISITOR");
    }

    private void updateMethodComplexity(MethodSignatures signatures, int amount) {
        Method m = signatures.get(methodDecl, methodName, className, packageName, filePath);

        int new_complexity = amount;
        Optional<MethodVersion> optionalVersion = m.getVersion(m.getNumberOfVersions() - 1);
        
        if (optionalVersion.isPresent()) {
            MethodVersion version = optionalVersion.get();
            Optional<Object> currComplexity = version.getProperty(CYCLOMATIC_ID);
            if (currComplexity.isPresent()) {
                new_complexity += (int) currComplexity.get();
            }
            version.setProperty(CYCLOMATIC_ID, new_complexity);
            signatures.add(m);
        }
    }

    @Override
    public void visit(MethodDeclaration method, MethodSignatures signatures) {
        this.methodDecl = method.getDeclarationAsString(false, false, false);
        this.methodName = method.getName().getIdentifier();
        this.updateMethodComplexity(signatures, 1);
        
        super.visit(method, signatures);
    }

    @Override
    public void visit(ForEachStmt statement, MethodSignatures signatures) {
        
        this.updateMethodComplexity(signatures, 1);

        super.visit(statement, signatures);
    }

    @Override
    public void visit(ForStmt statement, MethodSignatures signatures) {
        
        this.updateMethodComplexity(signatures, 1);

        super.visit(statement, signatures);
    }

    @Override
    public void visit(DoStmt statement, MethodSignatures signatures) {
        this.updateMethodComplexity(signatures, 1);

        super.visit(statement, signatures);
    }

    @Override
    public void visit(WhileStmt statement, MethodSignatures signatures) {
        this.updateMethodComplexity(signatures, 1);

        super.visit(statement, signatures);
    }

    @Override
    public void visit(IfStmt statement, MethodSignatures signatures) {
        this.updateMethodComplexity(signatures, 1);

        super.visit(statement, signatures);
    }

    @Override
    public void visit(SwitchStmt statement, MethodSignatures signatures) {
        // TODO how to handle switch statement, per case +1?
        this.updateMethodComplexity(signatures, 1);

        super.visit(statement, signatures);
    }
}
