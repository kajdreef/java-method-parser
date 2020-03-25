package org.spideruci.line.extractor;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.spideruci.line.extractor.parsers.MethodParser;
import org.spideruci.line.extractor.parsers.components.Component;
import org.spideruci.line.extractor.parsers.Parser;
import org.spideruci.line.extractor.util.DirectoryExplorer;

public class ParserLauncher {
    public List<Component> start(String projectDir) {
        // Create a parser
        Parser parser = new MethodParser();
        parser.setRootFolder(new File(projectDir));
        
        List<Component> methodSignatures = new LinkedList<>();

        // Get all source files in project
        for (File javaSourceFile : DirectoryExplorer.get(projectDir)) {
            // Parse files
            System.out.println(javaSourceFile.getAbsolutePath());
            methodSignatures.addAll(parser.parse(javaSourceFile.toPath()));
        }

        return methodSignatures;
    }

    public static void main(String[] args) {
        System.out.println("TEST");
        String projectDir = args[0];

        System.out.println(projectDir);
        
        List<Component> list = new ParserLauncher().start(projectDir);
        
        for (Component c : list) {
            System.out.println(c.asString());
        }
    }
}
