package com.kajdreef.analyzer.visitor;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;

import com.kajdreef.analyzer.metrics.MethodSignatures;


public abstract class AbstractMethodVisitor extends VoidVisitorAdapter<MethodSignatures> {
    protected String methodDecl = "";
    protected String className = "";
    protected String packageName = "";
    protected String filePath = "";
    
    protected final String VISITOR_NAME;

    public AbstractMethodVisitor(final String visitorName) {
        this.VISITOR_NAME = visitorName;
    }

    public String getVisitorName() {
        return VISITOR_NAME;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void visit(PackageDeclaration p, MethodSignatures m) {
        this.packageName = p.getNameAsString();
        super.visit(p, m);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration c, MethodSignatures m) {
        this.className = c.getNameAsString();
        super.visit(c, m);
    }

    @Override
    public void visit(EnumDeclaration c, MethodSignatures m) {
        this.className = c.getNameAsString();

        super.visit(c, m);
    }
}