package com.kajdreef.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kajdreef.analyzer.util.DirectoryExplorer;
import com.kajdreef.analyzer.visitor.AbstractMethodVisitor;
import com.kajdreef.analyzer.visitor.MethodCyclomaticComplexityVisitor;
import com.kajdreef.analyzer.visitor.MethodSignatureVisitor;
import com.kajdreef.analyzer.visitor.Components.Method;
import com.kajdreef.analyzer.metrics.*;

public class Analyzer {

    private List<AbstractMethodVisitor> visitors;
    private MethodSignatures signatures;

    public Analyzer() {
        this.visitors = new ArrayList<>();
        this.signatures = new MethodSignatures();
    }

    public Analyzer addVisitor(AbstractMethodVisitor visitor) {
        this.visitors.add(visitor);
        return this;
    }

    public void parse(File file) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);

            for (AbstractMethodVisitor visitor : this.visitors) {
                visitor.setFilePath(file.toString());
                visitor.visit(cu, this.signatures);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch(ParseProblemException e) {
            System.out.println("Parsing error in file: " + file.toString());
        }
    }

    public Map<Integer, Method> getMap() {
        return this.signatures.getMap();
    }

    public Set<Method> getMethodSet() {
        return this.signatures.getMap().values().stream().collect(Collectors.toSet());
    }

    public void outputToFile(boolean pp) {
        Gson gson;
        if (pp) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        } else {
            gson = new GsonBuilder().create();
        }

        Map<Integer, Method> ccMap = this.getMap();

        for (int key : ccMap.keySet()) {
            System.out.println(gson.toJson(ccMap.get(key)));
        }
    }

    public Analyzer analyzeDirectory(String projectDir) {
        for (File file : DirectoryExplorer.get(projectDir)) {
            this.parse(file);
        }
        return this;
    }

    public static void main(String[] args) throws FileNotFoundException {
        String projectDir = "/Users/kajdreef/code/research-scripts/projects/commons-io/";

        // Initialize the analyzer
        Analyzer analyzer = new Analyzer();

        // Create the visitors we are going to use
        MethodSignatureVisitor ms_visitor = new MethodSignatureVisitor();
        MethodCyclomaticComplexityVisitor cc_visitor = new MethodCyclomaticComplexityVisitor();

        // Add them to the analyzer
        analyzer.addVisitor(ms_visitor);
        analyzer.addVisitor(cc_visitor);

        analyzer.analyzeDirectory(projectDir);

        analyzer.outputToFile(true);
    }
}
