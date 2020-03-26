package org.spideruci.line.extractor.parsers;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Path;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
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
            

            String methodNameStr = method.getName().getIdentifier();
            List<String> parameters = method.getSignature().getParameterTypes().stream().map(p->p.asString()).collect(Collectors.toList());
        
            String returnTypeStr = returnType.asString();
            int lineRangeStart = method.getRange().get().begin.line;
            int lineRangeEnd = method.getRange().get().end.line;
            String filePathStr = this.rootDirectory.toPath().relativize(javaFilePath).toString();
            String classNameStr;

            if (node instanceof NodeWithSimpleName<?>) {  
                NodeWithSimpleName<Node> simpleNode = (NodeWithSimpleName<Node>) method.getParentNode().get();
                classNameStr = simpleNode.getNameAsString();
                method_list.add(new MethodSignature(filePathStr, classNameStr, methodNameStr, returnTypeStr, parameters,
                        lineRangeStart, lineRangeEnd));
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
