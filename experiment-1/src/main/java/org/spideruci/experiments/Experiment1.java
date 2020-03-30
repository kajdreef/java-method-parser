package org.spideruci.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spideruci.history.slicer.slicers.HistorySlicer;
import org.spideruci.history.slicer.slicers.HistorySlicerBuilder;
import org.spideruci.line.extractor.ParserLauncher;
import org.spideruci.line.extractor.parsers.components.Component;
import org.spideruci.line.extractor.parsers.components.MethodSignature;

public class Experiment1 {

    private String projectPath;
    private Repository repo;
    private Git git;
    private String pastCommit;
    private String presentCommit;
    private boolean allChanges = false;
    private String outputDir;

    private Logger logger = LoggerFactory.getLogger(Experiment1.class);

    private Gson gson;
    private Map<MethodSignature, List<String>> methodCommitMap;
    private Map<String, String> properties;

    public Experiment1() {
        gson = new GsonBuilder()
            // .setPrettyPrinting()
            .create();

        outputDir = ".";
        properties = new HashMap<>();
        methodCommitMap = new HashMap<>();
    }

    public Experiment1 setAllChanges() {
        allChanges = true;
        return this;
    }

    public Experiment1 setProject(String projectPath) {
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

    public Experiment1 setCommitRange(String pastCommit, String presentCommit) {
        this.pastCommit = pastCommit;
        this.presentCommit = presentCommit;
        return this;
    }

    public Set<Component> intersectionList(Set<Component> set1, Set<Component> set2) {
        return set2.stream().filter(set1::contains).collect(Collectors.toSet());
    }

    public Experiment1 run() throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException,
            CheckoutConflictException, GitAPIException {
        logger.info("Config - sut: {}, past-commit: {}, present-commit: {}", this.projectPath, this.pastCommit,
                this.presentCommit);

        properties.put("sut", this.projectPath);
        properties.put("present-commit", this.presentCommit);
        properties.put("past-commit", this.pastCommit);
        properties.put("allchanges", Boolean.toString(this.allChanges));

        // A function to filter out test code.
        Predicate<Component> testFilter = c -> {
            if (c instanceof MethodSignature) {
                MethodSignature m = (MethodSignature) c;
                return ! m.file_path.contains("test");
            } else {
                return false;
            }
        };

        // Get methods that appear in both snapshots
        git.checkout().setName(this.pastCommit).setForced(true).call();
        Set<Component> pastMethodSet = new ParserLauncher()
            .start(this.projectPath)
            .stream()
                .filter(testFilter)
                .collect(Collectors.toSet());

        git.reset().setMode(ResetType.HARD).call();
        // git.checkout().setName("master").setForced(true).call();

        git.checkout().setName(this.presentCommit).setForced(true).call();
        Set<Component> presentMethodSet = new ParserLauncher()
            .start(this.projectPath)
            .stream()
                .filter(testFilter)
                .collect(Collectors.toSet());

        // Get the intersection
        Set<Component> intersection = intersectionList(pastMethodSet, presentMethodSet);

        logger.info("past: {}, present: {}, intersection: {}", pastMethodSet.size(), presentMethodSet.size(),
                intersection.size());

        assert intersection.size() <= presentMethodSet.size();
        assert intersection.size() <= pastMethodSet.size();

        // Get the number of times a method was changed
        HistorySlicer slicer = HistorySlicerBuilder.getInstance().setForwardSlicing(false).build(this.repo);

        if (!allChanges) {
            slicer.setCommitRange(this.pastCommit, this.presentCommit);
        }

        for (Component c : intersection) {
            if (c instanceof MethodSignature) {
                MethodSignature m = (MethodSignature) c;
                List<String> list = slicer.trace(m.file_path, m.line_start, m.line_end);
                methodCommitMap.put(m, list);

                // if (list.size() > 0)
                logger.debug("{} - {}", m.asString(), list.size());
            }
        }

        git.reset().setMode(ResetType.HARD).call();
        git.checkout().setName("master").call();

        return this;
    }

    public Experiment1 setOutputDir(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    public Experiment1 report() {
        String[] pathSplit = projectPath.split("/");

        File outputDirFile = new File(this.outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }

        String outputFileName;
        if (this.allChanges) {
            outputFileName = String.format("%s-allchanges-%s-%s.json", pathSplit[pathSplit.length - 1], this.presentCommit, this.pastCommit);
        } else {
            outputFileName = String.format("%s-%s-%s.json", pathSplit[pathSplit.length - 1], this.presentCommit, this.pastCommit);
        }

        File report = new File(
            outputDirFile,
            outputFileName
        );

        JsonObject content = new JsonObject();
        
        for (Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            content.addProperty(key, value);
        }

        JsonArray methods = new JsonArray();
        for (Entry<MethodSignature, List<String>> entry : methodCommitMap.entrySet()) {
            MethodSignature m = entry.getKey();
            List<String> commits = entry.getValue();
            JsonElement mJson = gson.toJsonTree(m);

            mJson.getAsJsonObject().add("commits-sha", gson.toJsonTree(commits));
            mJson.getAsJsonObject().addProperty("commits-count", commits.size());

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
        options.addOption("o", "output", true, "Output directory");
        options.addOption("all", "allchanges", false, "Instead of only getting the changes for each method in the range of past and present. Get all the changes from present till beginning of time.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // Initialize the experiment and set all the parameters
        Experiment1 experiment1 = new Experiment1();

        if (cmd.hasOption("sut") && cmd.hasOption("past") && cmd.hasOption("present")) {
            experiment1.setProject(cmd.getOptionValue("sut"));
            experiment1.setCommitRange(cmd.getOptionValue("past"), cmd.getOptionValue("present"));
        }
        else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CLITester", options);
            System.exit(1);
        }

        if (cmd.hasOption("allchanges")) {
            experiment1.setAllChanges();
        }

        if (cmd.hasOption("output")) {
            experiment1.setOutputDir(cmd.getOptionValue("output"));
        }

        // Run the experiment
        try{
            experiment1.run()
                .report();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}