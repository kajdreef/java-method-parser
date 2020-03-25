package org.spideruci.history.slicer.slicers;

import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public abstract class HistorySlicer {
    protected Repository repo;
    protected Git git;

    HistorySlicer(Repository repo) {
        this.repo = repo;
        this.git = new Git(repo);
    }

    abstract public List<String> trace(String filePath);
    // abstract public List<String> trace(String filePath, AnyObjectId since, AnyObjectId until);
    abstract public List<String> trace(String filePath, int start_line, int end_line);
    // abstract public List<String> trace(String filePath, int start_line, int end_line, AnyObjectId since, AnyObjectId until);
}
