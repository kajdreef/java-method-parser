package org.spideruci.history.slicer.slicers;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

public abstract class HistorySlicer {
    protected Repository repo;
    protected Git git;
    protected ObjectId pastCommit, presentCommit;

    HistorySlicer(Repository repo) {
        this.repo = repo;
        this.git = new Git(repo);
    }

    public HistorySlicer setCommitRange(String pastCommit, String presentCommit) {
        try {
            this.pastCommit = this.repo.resolve(pastCommit);
            this.presentCommit = this.repo.resolve(presentCommit);
            
        } catch (RevisionSyntaxException | NullPointerException | IOException e) {
            System.out.println("[Commit not found] present:" +  this.presentCommit + " - past: " + this.pastCommit);
        } 

        return this;
    }

    abstract public Map<String, Object> trace(String filePath);
    abstract public Map<String, Object> trace(String filePath, int start_line, int end_line);
}
