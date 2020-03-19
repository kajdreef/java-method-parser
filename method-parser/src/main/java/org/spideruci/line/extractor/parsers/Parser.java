package org.spideruci.line.extractor.parsers;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.spideruci.line.extractor.parsers.components.Component;

public abstract class Parser {
    protected File rootDirectory;
    
    public void setRootFolder(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    abstract public List<Component> parse(Path javaPath);
}