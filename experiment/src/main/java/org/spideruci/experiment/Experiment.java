package org.spideruci.experiment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kajdreef.analyzer.Analyzer;
import com.kajdreef.analyzer.visitor.MethodCyclomaticComplexityVisitor;
import com.kajdreef.analyzer.visitor.MethodSignatureVisitor;
import com.kajdreef.analyzer.visitor.Components.Method;
import com.kajdreef.analyzer.visitor.Components.MethodVersion;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spideruci.history.slicer.slicers.HistorySlicer;
import org.spideruci.history.slicer.slicers.HistorySlicerBuilder;

public class Experiment {

    private String projectPath;
    private Repository repo;
    private Git git;
    private String pastCommit;
    private String presentCommit;

    private Logger logger = LoggerFactory.getLogger(Experiment.class);

    public Experiment(String projectPath, String pastCommit, String presentCommit) {
        this.projectPath = projectPath;
        this.pastCommit = pastCommit;
        this.presentCommit = presentCommit;

        File repoDirectory = new File(projectPath + File.separator + ".git");

        try {
            this.repo = new FileRepositoryBuilder().setGitDir(repoDirectory).build();
            this.git = new Git(repo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * New all versions are added to presentMethod.
     */
    private Set<Method> intersectionList(Set<Method> pastSet, Set<Method> presentSet) {
        Set<Method> result = new HashSet<>();

        for (Method presentMethod : presentSet) {
            for (Method pastMethod : pastSet) {
                if (presentMethod.equals(pastMethod)) {
                    for (MethodVersion version : pastMethod.versions) {
                        presentMethod.addVersion(version);
                    }
                    result.add(presentMethod);
                    break;
                }
            }
        }
        return result;
    }

    private Set<Method> parseProject(Predicate<Method> filter) {
        // Create the visitors we are going to use
        MethodSignatureVisitor ms_visitor = new MethodSignatureVisitor();
        MethodCyclomaticComplexityVisitor cc_visitor = new MethodCyclomaticComplexityVisitor();

        Set<Method> methodSet = new Analyzer()
            .addVisitor(ms_visitor)
            .addVisitor(cc_visitor)
                .analyzeDirectory(this.projectPath).getMethodSet();

        if (filter != null) {
            return methodSet.stream().filter(filter).collect(Collectors.toSet());
        }

        return methodSet;
    }

    private Set<Method> getMethods(Predicate<Method> filter, String commitSHA) throws GitAPIException {
        if (commitSHA != null) {
            git.checkout().setName(commitSHA).setForced(true).call();
        }

        Set<Method> methods = this.parseProject(filter);

        methods.stream().forEach((m) -> {
            Optional<MethodVersion> optionalVersion = m.getVersion(0);
            if (optionalVersion.isPresent() && commitSHA != null) {
                MethodVersion version = optionalVersion.get();
                version.commitId = commitSHA;
                version.commitDate = getDateFromCommit(commitSHA).get();
            }
        });

        git.reset().setMode(ResetType.HARD).call();

        return methods;
    }

    private Set<Method> getMethodsInBothCommits(Predicate<Method> filter) throws GitAPIException {
        Set<Method> result;

        // Get methods that appear in both snapshots
        Set<Method> pastMethodSet = getMethods(null, this.pastCommit);
        Set<Method> presentMethodSet = getMethods(null, this.presentCommit);

        result = intersectionList(pastMethodSet, presentMethodSet);

        logger.info("past: {}, present: {}, intersection: {}", pastMethodSet.size(), presentMethodSet.size(),
                result.size());

        assert result.size() <= presentMethodSet.size();
        assert result.size() <= pastMethodSet.size();

        if (result.size() == 0) {
            logger.info("No methods in common found between these two projects");
            System.exit(0);
        }

        return result;
    }

    private Optional<String> getDateFromCommit(String commit) {
        try {
            ObjectId oid = this.repo.resolve(commit);
            RevCommit revCommit = this.repo.parseCommit(oid);
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
                .format(new Date(revCommit.getCommitTime() * 1000L));
            
            return Optional.of(date);

        } catch(IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Map<String, String>> getHistoryMethod(HistorySlicer slicer, Method m) {
        MethodVersion version = m.getVersion(0).get();
        Map<String, Object> historyProperties = slicer.trace(m.filePath, version.lineStart, version.lineEnd);
        List<Map<String, String>> finalList = new LinkedList<>();
        List<String> commits = new LinkedList<>();

        Object commitsObj = historyProperties.get("commits");
        if (commitsObj instanceof List<?>) {
            commits = (List<String>) commitsObj;
        }
        
        for (String commit : commits) {
            Optional<String> result = getDateFromCommit(commit);

            if (! result.isPresent()) {
                continue;
            }
            String date = result.get();
            Map<String, String> commitData = new HashMap<>();
            commitData.put("commit", commit);
            commitData.put("date", date);

            finalList.add(commitData);
        }
        return finalList;
    }

    public Map<String, Set<Method>> run() throws GitAPIException {
        Map<String, Set<Method>> result = new HashMap<>();

        logger.info(
            "Config - sut: {}, past-commit: {}, present-commit: {}", 
            this.projectPath,
            this.pastCommit,
            this.presentCommit
        );

        // A function to filter out test code.
        // Predicate<Method> testFilter = m -> {
        //     return !m.filePath.contains("test");
        // };
        Set<Method> methodSet;
        if (this.pastCommit == null) {
            methodSet = getMethods(null, this.presentCommit);
        }
        else {
            methodSet = getMethodsInBothCommits(null);
        }

        // Get the number of times a method was changed
        HistorySlicer slicer = HistorySlicerBuilder.getInstance()
            .build(this.repo)
            .setCommitRange(this.pastCommit, this.presentCommit);

        methodSet.stream().forEach((m) -> {
            // m.getRight() == present method
            m.setHistory(getHistoryMethod(slicer, m));
        });

        Set<Method> prodMethods = methodSet.stream()
            .filter((m) -> !m.filePath.contains("test"))
            .collect(Collectors.toSet());

        Set<Method> testMethods = methodSet.stream()
            .filter((m) -> m.filePath.contains("test"))
            .collect(Collectors.toSet());

        result.put("test-methods", testMethods);
        result.put("prod-methods", prodMethods);

        git.reset().setMode(ResetType.HARD).call();
        if (this.pastCommit != null || this.presentCommit != null){
            git.checkout().setName("master").call();
        }
        System.out.println(testMethods.size());
        System.out.println(prodMethods.size());
        return result;
    }

    public static void main(String[] args) throws ParseException {
        
        // Create command line interface 
        Options options = new Options();
        options.addOption("s", "sut", true, "Path to the system under study.");
        options.addOption("o", "output", true, "Output file path");

        // Optional
        options.addOption("pa", "past", true, "Starting commit from which the experiment starts.");
        options.addOption("pr", "present", true, "Starting commit from which the experiment starts.");
        

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // Check if all parameters were set
        if (! cmd.hasOption("sut") && cmd.hasOption("past")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CLITester", options);
            System.exit(64);
        }

        // Initialize the experiment and set all the parameters
        Experiment experiment;
        if (cmd.hasOption("present") && cmd.hasOption("output")) {
            experiment = new Experiment(
                cmd.getOptionValue("sut"),
                cmd.getOptionValue("past"),
                cmd.getOptionValue("present")
            );
        }
        else {
            experiment = new Experiment(
                cmd.getOptionValue("sut"),
                null,
                null
            );
        }
        Map<String, Set<Method>> experiment_data;
        try {
            experiment_data = experiment.run();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        // Create output map (and set some additional data about experiment)
        Map<String, Object> jsonOutput = new HashMap<>();
        jsonOutput.put("sut", cmd.getOptionValue("sut"));

        if (cmd.hasOption("past") && cmd.hasOption("present")) {
            jsonOutput.put("past-commit", cmd.getOptionValue("past"));
            jsonOutput.put("past-commit-date", experiment.getDateFromCommit(cmd.getOptionValue("past")).get());

            jsonOutput.put("present-commit", cmd.getOptionValue("present"));
            jsonOutput.put("present-commit-date", experiment.getDateFromCommit(cmd.getOptionValue("present")).get());
        }
        
        try {
            jsonOutput.put("methods", experiment_data);

            File report = new File(cmd.getOptionValue("output"));
            File parentDir = report.getParentFile();

            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Write data to file
            FileWriter writer = new FileWriter(report);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonOutput, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}