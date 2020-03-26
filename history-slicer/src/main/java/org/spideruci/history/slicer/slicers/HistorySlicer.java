package org.spideruci.history.slicer.slicers;

import java.io.IOException;
import java.util.List;

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
            this.presentCommit = this.repo.resolve(pastCommit);
            
        } catch (RevisionSyntaxException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }

    abstract public List<String> trace(String filePath);
    // abstract public List<String> trace(String filePath, AnyObjectId since, AnyObjectId until);
    abstract public List<String> trace(String filePath, int start_line, int end_line);
    // abstract public List<String> trace(String filePath, int start_line, int end_line, AnyObjectId since, AnyObjectId until);
}
