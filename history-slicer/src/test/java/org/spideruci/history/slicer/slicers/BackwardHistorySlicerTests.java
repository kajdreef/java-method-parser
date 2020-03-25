package org.spideruci.history.slicer.slicers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ForwardHistorySlicerTests {

    @Test
    public void traceNumberOfCommitsTouchedFile() {
        // Given project and file
        String project = "/Users/kajdreef/code/dummy-projects/git-log-file";
        String filePath = "hello.cpp";

        // When: tracing the evolution of the file and line range
        try {
            Repository repo = new RepositoryBuilder()
                .setGitDir(new File(project, ".git"))
                .build();

            HistorySlicer slicer = HistorySlicerBuilder.getInstance()
                .setForwardSlicing(false)
                .build(repo);

            List<String> commits = slicer.trace(filePath);

            // Then: three commits should touch those lines of code.
            Assertions.assertTrue(commits.size() == 4);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void traceChangesDoneToASingleMethod(){
        // Given project, file, and line range.
        String project = "/Users/kajdreef/code/dummy-projects/git-log-file";
        String filePath = "hello.cpp";
        int start = 5, end = 10;

        // When: tracing the evolution of the file and line range
        try {
            Repository repo = new RepositoryBuilder()
                .setGitDir(new File(project, ".git"))
                .build();

            HistorySlicer slicer = HistorySlicerBuilder.getInstance()
                .setForwardSlicing(false)
                .build(repo);

            List<String> commits = slicer.trace(filePath, start, end);

            // Then: three commits should touch those lines of code.
            Assertions.assertTrue(commits.size() == 4);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void traceChangesDoneToSingleLine() {
        // Given project, file, and a single line
        String project = "/Users/kajdreef/code/dummy-projects/git-log-file";
        String filePath = "hello.cpp";
        int start = 3, end = 3;

        // When: tracing the evolution of a single line
        try {
            Repository repo = new RepositoryBuilder()
                .setGitDir(new File(project, ".git"))
                .build();

            HistorySlicer slicer = HistorySlicerBuilder.getInstance()
                .setForwardSlicing(false)
                .build(repo);

            List<String> commits = slicer.trace(filePath, start, end);

            // Then: One commit should touch those lines of code.
            Assertions.assertTrue(commits.size() == 1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}