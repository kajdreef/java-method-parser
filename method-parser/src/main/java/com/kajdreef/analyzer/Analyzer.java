package com.kajdreef.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
import com.google.gson.JsonIOException;
import com.kajdreef.analyzer.util.DirectoryExplorer;
import com.kajdreef.analyzer.visitor.AbstractMethodVisitor;
import com.kajdreef.analyzer.visitor.MethodCyclomaticComplexityVisitor;
import com.kajdreef.analyzer.visitor.MethodSignatureVisitor;
import com.kajdreef.analyzer.visitor.Components.Method;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.kajdreef.analyzer.metrics.*;

public class Analyzer {

    private List<AbstractMethodVisitor> visitors;
    private MethodSignatures signatures;
    private String sut = "";

    public Analyzer() {
        this.visitors = new ArrayList<>();
        this.signatures = new MethodSignatures();
    }

    public void setSut(String sut){
        this.sut = sut;
    }

    public Analyzer addVisitor(AbstractMethodVisitor visitor) {
        this.visitors.add(visitor);
        return this;
    }

    public void parse(File file) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);

            for (AbstractMethodVisitor visitor : this.visitors) {
                String filePath = file.toString();

                if (filePath.contains(this.sut)) {
                    filePath = filePath.replace(this.sut, "");
                }
                visitor.setFilePath(filePath);
                visitor.visit(cu, this.signatures);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseProblemException e) {
            System.out.println("Parsing error in file: " + file.toString());
        }
    }

    public Map<Integer, Method> getMap() {
        return this.signatures.getMap();
    }

    public Set<Method> getMethodSet() {
        return this.signatures.getMap().values().stream().collect(Collectors.toSet());
    }

    public void outputToFile(String outputPath, boolean pp) {
        Gson gson;
        if (pp) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        } else {
            gson = new GsonBuilder().create();
        }

        Map<Integer, Method> ccMap = this.getMap();
        
        // Create the file object and see if the parents path exists, it not create the parent directories.
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();

        if (! parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Write json object to file.
        try {
            FileWriter fileWriter = new FileWriter(outputFile);
            fileWriter.append('[');
            int i = 0;
            for (Method method : ccMap.values()) {
                gson.toJson(method, fileWriter);
                i++;
                if(i <= ccMap.size() - 1){
                    fileWriter.append(',');
                }
            }
    
            fileWriter.append(']');
            fileWriter.flush();
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
        }
    }

    public Analyzer analyzeDirectory(String projectDir) {
        for (File file : DirectoryExplorer.get(projectDir)) {
            this.parse(file);
        }
        return this;
    }

    public static void main(String[] args) throws FileNotFoundException, ParseException {
        Options options = new Options();
        options.addOption("s", "sut", true, "Path to the system under study.");
        options.addOption("o", "outputPath", true, "Output file path");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);


        if (! (options.hasOption("sut") && options.hasOption("outputPath"))) {
            System.exit(1);
        }
        String projectDir = cmd.getOptionValue("sut");
        String outputPath = cmd.getOptionValue("outputPath");

        // Initialize the analyzer
        Analyzer analyzer = new Analyzer();
        
        analyzer.setSut(projectDir);

        // Create the visitors we are going to use
        MethodSignatureVisitor ms_visitor = new MethodSignatureVisitor();
        MethodCyclomaticComplexityVisitor cc_visitor = new MethodCyclomaticComplexityVisitor();

        // Add them to the analyzer
        analyzer.addVisitor(ms_visitor);
        analyzer.addVisitor(cc_visitor);

        analyzer.analyzeDirectory(projectDir);

        analyzer.outputToFile(outputPath, true);
    }
}
