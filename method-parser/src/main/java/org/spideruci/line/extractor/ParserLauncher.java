package org.spideruci.line.extractor;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.spideruci.line.extractor.parsers.MethodParser;
import org.spideruci.line.extractor.parsers.components.Component;
import org.spideruci.line.extractor.parsers.Parser;
import org.spideruci.line.extractor.util.DirectoryExplorer;

public class ParserLauncher {
    public Set<Component> start(String projectDir) {
        // Create a parser
        Parser parser = new MethodParser();
        parser.setRootFolder(new File(projectDir));
        
        Set<Component> methodSignatures = new HashSet<>();

        // Get all source files in project
        for (File javaSourceFile : DirectoryExplorer.get(projectDir)) {
            // Parse files
            methodSignatures.addAll(parser.parse(javaSourceFile.toPath()));
        }

        return methodSignatures;
    }

    public static void main(String[] args) {
        String projectDir = args[0];

        System.out.println(projectDir);
        
        Set<Component> set = new ParserLauncher().start(projectDir);
        
        for (Component c : set) {
            System.out.println(c.asString());
        }
    }
}
