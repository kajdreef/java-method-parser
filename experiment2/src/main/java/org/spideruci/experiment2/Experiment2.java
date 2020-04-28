package org.spideruci.experiment2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kajdreef.analyzer.Analyzer;
import com.kajdreef.analyzer.visitor.MethodCyclomaticComplexityVisitor;
import com.kajdreef.analyzer.visitor.MethodSignatureVisitor;
import com.kajdreef.analyzer.visitor.Components.Method;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spideruci.history.slicer.slicers.HistorySlicer;
import org.spideruci.history.slicer.slicers.HistorySlicerBuilder;

public class Experiment2 {

    private String projectPath;
    private Repository repo;
    private Git git;
    private String pastCommit;
    private String presentCommit;
    private String outputPath;

    private Logger logger = LoggerFactory.getLogger(Experiment2.class);

    private Gson gson;
    private Map<Method, List<Pair<String, String>>> methodCommitMap;
    private Map<String, String> properties;

    public Experiment2() {
        gson = new GsonBuilder().setPrettyPrinting().create();

        outputPath = ".";
        properties = new HashMap<>();
        methodCommitMap = new HashMap<>();
    }

    public Experiment2 setProject(String projectPath) {
        this.projectPath = projectPath;
        File repoDirectory = new File(projectPath + File.separator + ".git");

        try {
            this.repo = new FileRepositoryBuilder().setGitDir(repoDirectory).build();
            this.git = new Git(repo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public Experiment2 setCommitRange(String pastCommit, String presentCommit) {
        this.pastCommit = pastCommit;
        this.presentCommit = presentCommit;
        return this;
    }

    public Set<Method> intersectionList(Set<Method> set1, Set<Method> set2) {
        return set1.stream().filter(set2::contains).collect(Collectors.toSet());
    }

    public Experiment2 run() throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException,
            CheckoutConflictException, GitAPIException {
        logger.info("Config - sut: {}, past-commit: {}, present-commit: {}", this.projectPath, this.pastCommit,
                this.presentCommit);

        properties.put("sut", this.projectPath);
        properties.put("present-commit", this.presentCommit);
        properties.put("past-commit", this.pastCommit);

        // A function to filter out test code.
        Predicate<Method> testFilter = m -> {
            return !m.filePath.contains("test");
        };

        // Create the visitors we are going to use
        MethodSignatureVisitor ms_visitor = new MethodSignatureVisitor();
        MethodCyclomaticComplexityVisitor cc_visitor = new MethodCyclomaticComplexityVisitor();


        // Get methods that appear in both snapshots
        git.checkout().setName(this.pastCommit).setForced(true).call();
        Set<Method> pastMethodSet = new Analyzer()
            .addVisitor(ms_visitor)
            .addVisitor(cc_visitor)
            .analyzeDirectory(this.projectPath)
            .getMethodSet()
            .stream()
                .filter(testFilter)
                .collect(Collectors.toSet());

        git.reset().setMode(ResetType.HARD).call();

        git.checkout().setName(this.presentCommit).setForced(true).call();
        Set<Method> presentMethodSet = new Analyzer()
            .addVisitor(ms_visitor)
            .addVisitor(cc_visitor)
            .analyzeDirectory(this.projectPath)
            .getMethodSet()
            .stream()
                .filter(testFilter)
                .collect(Collectors.toSet());

        // Get the intersection (KEEP PRESENT COMMIT)
        Set<Method> intersection = intersectionList(presentMethodSet, pastMethodSet);

        logger.info("past: {}, present: {}, intersection: {}", pastMethodSet.size(), presentMethodSet.size(),
                intersection.size());

        assert intersection.size() <= presentMethodSet.size();
        assert intersection.size() <= pastMethodSet.size();

        if (intersection.size() == 0) {
            logger.info("No methods in common found between these two projects");
            System.exit(0);
        }

        // Get the number of times a method was changed
        HistorySlicer slicer = HistorySlicerBuilder.getInstance()
            .build(this.repo);

        slicer.setCommitRange(this.pastCommit, this.presentCommit);

        for (Method m : intersection) {

            Map<String, Object> properties = slicer.trace(m.filePath, m.lineStart, m.lineEnd);
            List<Pair<String, String>> finalList = new LinkedList<>();
            List<String> commits;
            Object commitsObj = properties.get("commits");
            int totalCommits = Integer.parseInt((String) properties.get("total_commits"));
            // int totalChurn = Integer.parseInt((String) properties.get("code_churn"));
            
            // double averageChurn = 0;
            // if (totalCommits != 0) {
            //     averageChurn = (double) totalChurn / totalCommits;
            // }

            m.addProperty("commits_in_window", totalCommits);
            // m.addProperty("code_churn", totalChurn);
            // m.addProperty("averageChurn", Double.toString(averageChurn));

            if (commitsObj instanceof List<?>) {
                commits = (List<String>) commitsObj;
            }
            else {
                commits = new LinkedList<>();
            }
            
            for (String commit : commits) {
                try {
                    ObjectId oid = this.repo.resolve(commit);
                    RevCommit revCommit = this.repo.parseCommit(oid);
                    String date = new SimpleDateFormat("yyyy-MM-dd")
                            .format(new Date(revCommit.getCommitTime() * 1000L));
                    finalList.add(new ImmutablePair<String, String>(commit, date));
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }

            methodCommitMap.put(m, finalList);
        }

        git.reset().setMode(ResetType.HARD).call();
        git.checkout().setName("master").call();

        return this;
    }

    public Experiment2 setOutputPath(String outputPath) {
        this.outputPath = outputPath;
        return this;
    }

    public Experiment2 report() {
        File report = new File(this.outputPath);
        File parentDir = report.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        JsonObject content = new JsonObject();

        for (Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            content.addProperty(key, value);
        }

        JsonArray methods = new JsonArray();
        for (Entry<Method, List<Pair<String, String>>> entry : methodCommitMap.entrySet()) {
            Method m = entry.getKey();
            List<Pair<String, String>> commits = entry.getValue();
            JsonElement mJson = gson.toJsonTree(m);
            JsonArray commitsJson = new JsonArray();

            for (Pair<String, String> commit : commits) {
                JsonObject commitJson = new JsonObject();
                commitJson.addProperty("SHA1", commit.getLeft());
                commitJson.addProperty("date", commit.getRight());
                commitsJson.add(commitJson);
            }

            mJson.getAsJsonObject().add("commits", commitsJson);

            methods.add(mJson);
        }
        content.add("methods", methods);

        try {
            FileWriter writer = new FileWriter(report);
            gson.toJson(content, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public static void main(String[] args) throws ParseException {
        Options options = new Options();

        options.addOption("s", "sut", true, "Path to the system under study.");
        options.addOption("pa", "past", true, "Starting commit from which the experiment starts.");
        options.addOption("pr", "present", true, "Starting commit from which the experiment starts.");
        options.addOption("o", "output", true, "Output file path");
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // Initialize the experiment and set all the parameters
        Experiment2 Experiment2 = new Experiment2();

        if (cmd.hasOption("sut") && cmd.hasOption("past") && cmd.hasOption("present")) {
            Experiment2.setProject(cmd.getOptionValue("sut"));
            Experiment2.setCommitRange(cmd.getOptionValue("past"), cmd.getOptionValue("present"));
        } else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CLITester", options);
            System.exit(64);
        }


        if (cmd.hasOption("output")) {
            Experiment2.setOutputPath(cmd.getOptionValue("output"));
        } else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CLITester", options);
            System.exit(1);
        }

        // Run the experiment
        try {
            Experiment2.run().report();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}