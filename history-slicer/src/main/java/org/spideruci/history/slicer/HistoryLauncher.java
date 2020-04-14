package org.spideruci.history.slicer;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spideruci.history.slicer.slicers.HistorySlicer;
import org.spideruci.history.slicer.slicers.HistorySlicerBuilder;

public class HistoryLauncher {

    private final Logger logger = LoggerFactory.getLogger(HistoryLauncher.class);
    private Git git;
    private Repository repo;

    public HistoryLauncher() {
    }

    public HistoryLauncher initializeRepo(String project ) {
        // Initialize repository
        logger.info("Project: {}", project);

        File repoDirectory = new File(project + File.separator + ".git");
        if (repoDirectory.exists()) {
            logger.info("Repository exists");
        } else {
            logger.info("Repository does not exist...");
            System.exit(1);
        }

        try {
            this.repo = new FileRepositoryBuilder().setGitDir(repoDirectory).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.git = new Git(repo);

        return this;
    }

    public void start() {
        try {
            //  Initialize slicer
            HistorySlicer slicer = HistorySlicerBuilder.getInstance()
                .build(this.repo);

            // Get the correct content 
            Ref ref = repo.findRef(Constants.HEAD);

            if (ref == null) {
                logger.info("Reference not found.");
                System.exit(1);
            }

            // Get all java files
            List<File> javaFiles = this.getFiles(repo, ref);

            for (File f: javaFiles) {
                System.out.println(f.toString());
                Map<String, Object> properties = slicer.trace(f.toString());
                properties.forEach((key, value) -> System.out.println(key + " - " + value.toString()));
                System.out.println("");
            }

            git.close();
        } catch (IOException e) {

        }
    }

    private List<File> getFiles(Repository repo, Ref ref) {
        List<File> result = new LinkedList<>();
        RevTree tree;
        try {
            RevCommit commit = repo.parseCommit(ref.getObjectId());
            tree = commit.getTree();
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }

        try(TreeWalk treeWalk = new TreeWalk(repo)) {
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);

            while (treeWalk.next()) {
                if (treeWalk.isSubtree()) {
                    treeWalk.enterSubtree();
                } else {
                    String path = treeWalk.getPathString();
                    
                    if (path.endsWith("java")) {
                        logger.info("File in repo: {}", path);
                        result.add(new File(path));
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void main(String[] args) {


        new HistoryLauncher()
            .initializeRepo(args[0])
            .start();
    }
}
