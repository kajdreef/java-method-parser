package org.spideruci.line.extractor.parsers;

import java.util.LinkedList;
import java.util.List;
import java.nio.file.Path;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.type.Type;

import org.spideruci.line.extractor.parsers.components.*;

public class MethodParser extends Parser {


    public MethodParser() {}

    private CompilationUnit getCompilationUnit(Path javaFilePath) {
        CompilationUnit cu = null;
        try {
            cu = StaticJavaParser.parse(javaFilePath);
        } catch (Exception e) {
            System.err.println("failed to parse File: " + javaFilePath.toString());
        }
        return cu;
    }

    private List<Component> findAllMethodSignatures(CompilationUnit cu, Path javaFilePath) {
        List<Component> method_list = new LinkedList<>();
        cu.findAll(MethodDeclaration.class).stream().forEach((MethodDeclaration method) -> {
            Type returnType = method.getType();

            Node node = method.getParentNode().get();

            if (node instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration parentNode = (ClassOrInterfaceDeclaration) method.getParentNode().get();

                method_list.add(new MethodSignature(this.rootDirectory.toPath().relativize(javaFilePath).toString(),
                        parentNode.getNameAsString(), method.getSignature().asString(), returnType.asString(),
                        method.getRange().get().begin.line, method.getRange().get().end.line));
            } else if (node instanceof EnumDeclaration) {
                EnumDeclaration parentNode = (EnumDeclaration) method.getParentNode().get();
                method_list.add(new MethodSignature(this.rootDirectory.toPath().relativize(javaFilePath).toString(),
                    parentNode.getNameAsString(), method.getSignature().asString(), returnType.asString(),
                    method.getRange().get().begin.line, method.getRange().get().end.line)
                );
            }
        });

        return method_list;
    }

    

    public List<Component> parse(Path javaFilePath) {
        CompilationUnit cu = getCompilationUnit(javaFilePath);
        List<Component> method_list = new LinkedList<>();

        if (cu != null) {
            method_list = findAllMethodSignatures(cu, javaFilePath);   
        }

        return method_list;
    }
}
