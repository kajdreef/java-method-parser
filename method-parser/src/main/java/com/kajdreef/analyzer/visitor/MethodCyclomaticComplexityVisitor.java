package com.kajdreef.analyzer.visitor;

import com.github.javaparser.ast.body.MethodDeclaration;

import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;

import com.kajdreef.analyzer.metrics.*;
import com.kajdreef.analyzer.visitor.Components.Method;

public class MethodCyclomaticComplexityVisitor extends AbstractMethodVisitor {

    private final String CYCLOMATIC_ID = "cyclomatic_complexity";

    public MethodCyclomaticComplexityVisitor() {
        super("CC_VISITOR");
    }

    private void updateMethodComplexity(MethodSignatures signatures, int amount) {
        Method m = signatures.get(methodDecl, methodName, className, packageName, filePath);

        int new_complexity = amount;

        Object curr_complexity = m.getProperty(CYCLOMATIC_ID);
        if (curr_complexity != null) {
            new_complexity += (int) curr_complexity;
        }
        
        m.addProperty(CYCLOMATIC_ID, new_complexity);

        signatures.add(m);
    }

    // public void visit(ConstructorDeclaration method, MethodSignatures signatures){
        
    // }

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
