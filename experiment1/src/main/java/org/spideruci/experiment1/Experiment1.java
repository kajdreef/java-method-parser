// package org.spideruci.experiment1;

// import java.io.File;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.text.SimpleDateFormat;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.LinkedList;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.Map.Entry;
// import java.util.function.Predicate;
// import java.util.stream.Collectors;

// import com.google.gson.Gson;
// import com.google.gson.GsonBuilder;
// import com.google.gson.JsonArray;
// import com.google.gson.JsonElement;
// import com.google.gson.JsonObject;

// import org.apache.commons.cli.CommandLine;
// import org.apache.commons.cli.CommandLineParser;
// import org.apache.commons.cli.DefaultParser;
// import org.apache.commons.cli.HelpFormatter;
// import org.apache.commons.cli.Options;
// import org.apache.commons.cli.ParseException;
// import org.apache.commons.lang3.tuple.ImmutablePair;
// import org.apache.commons.lang3.tuple.Pair;
// import org.eclipse.jgit.api.Git;
// import org.eclipse.jgit.api.ResetCommand.ResetType;
// import org.eclipse.jgit.api.errors.CheckoutConflictException;
// import org.eclipse.jgit.api.errors.GitAPIException;
// import org.eclipse.jgit.api.errors.InvalidRefNameException;
// import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
// import org.eclipse.jgit.api.errors.RefNotFoundException;
// import org.eclipse.jgit.lib.ObjectId;
// import org.eclipse.jgit.lib.Repository;
// import org.eclipse.jgit.revwalk.RevCommit;
// import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.spideruci.history.slicer.slicers.HistorySlicer;
// import org.spideruci.history.slicer.slicers.HistorySlicerBuilder;
// import org.spideruci.line.extractor.ParserLauncher;
// import org.spideruci.line.extractor.parsers.components.Component;
// import org.spideruci.line.extractor.parsers.components.MethodSignature;

// public class Experiment1 {

//     private String projectPath;
//     private Repository repo;
//     private Git git;
//     private String commitID;
//     private String outputDir;

//     private Logger logger = LoggerFactory.getLogger(Experiment1.class);

//     private Gson gson;
//     private Map<MethodSignature, List<Pair<String, String>>> methodCommitMap;
//     private Map<String, String> properties;

//     public Experiment1() {
//         gson = new GsonBuilder()
//                 .setPrettyPrinting()
//                 .create();

//         outputDir = ".";
//         properties = new HashMap<>();
//         methodCommitMap = new HashMap<>();
//     }

//     public Experiment1 setProject(String projectPath, String commit) {
//         this.projectPath = projectPath;
//         this.commitID = commit;
//         File repoDirectory = new File(projectPath + File.separator + ".git");

//         try {
//             this.repo = new FileRepositoryBuilder().setGitDir(repoDirectory).build();
//             this.git = new Git(repo);
//             this.git.checkout().setName(commit).call();
//         } catch (IOException | GitAPIException e) {
//             e.printStackTrace();
//         }

//         return this;
//     }

//     public Experiment1 run() throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException,
//             CheckoutConflictException, GitAPIException {
//         logger.info("Config - sut: {}, commit-id: {}", this.projectPath, this.commitID);

//         properties.put("sut", this.projectPath);
//         properties.put("commit", this.commitID);

//         // A function to filter out test code.
//         Predicate<Component> productionFilter = c -> {
//             if (c instanceof MethodSignature) {
//                 MethodSignature m = (MethodSignature) c;
//                 return !m.file_path.contains("test");
//             } else {
//                 return false;
//             }
//         };

//         Predicate<Component> testFilter = c -> {
//             if (c instanceof MethodSignature) {
//                 MethodSignature m = (MethodSignature) c;
//                 // TODO add extends TestCase Java
//                 return m.containAnnotation("Test");
//             } else {
//                 return false;
//             }
//         };

//         // Get methods that appear in both snapshots
//         Set<Component> allMethods = new ParserLauncher().start(this.projectPath);

//         Set<Component> productionMethods = allMethods.stream()
//                 .filter(productionFilter)
//                 .collect(Collectors.toSet());

//         Set<Component> testMethods = allMethods.stream()
//                 .filter(testFilter)
//                 .collect(Collectors.toSet());

//         logger.info("Number of tests: {}", testMethods.size());
//         properties.put("number-of-tests", String.valueOf(testMethods.size()));

//         // Get the number of times a method was changed
//         HistorySlicer slicer = HistorySlicerBuilder.getInstance()
//             .build(this.repo);

//         for (Component c : productionMethods) {
//             if (c instanceof MethodSignature) {
//                 MethodSignature m = (MethodSignature) c;
//                 Map<String, Object> properties = slicer.trace(m.file_path, m.line_start, m.line_end);
//                 List<Pair<String, String>> finalList = new LinkedList<>();
//                 List<String> commits;

//                 Object commitsObj = properties.get("commits");

//                 String totalCommits = (String) properties.get("total_commits");
//                 String totalChurn = (String) properties.get("code_churn");
//                 double averageChurn = (double) Integer.parseInt(totalChurn) / Integer.parseInt(totalCommits);

//                 m.addMetricResult("total_commits", totalCommits);
//                 m.addMetricResult("code_churn", totalChurn);
//                 m.addMetricResult("averageChurn", Double.toString(averageChurn));

//                 if (commitsObj instanceof List<?>) {
//                     commits = (List<String>) commitsObj;
//                 } else {
//                     commits = new LinkedList<>();
//                 }

//                 for (String commit : commits) {
//                     try {
//                         ObjectId oid = this.repo.resolve(commit);
//                         RevCommit revCommit = this.repo.parseCommit(oid);
//                         String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(revCommit.getCommitTime() * 1000L));
//                         finalList.add(new ImmutablePair<String,String>(commit, date));
//                     } catch (IOException e) {
                     
//                         e.printStackTrace();
//                     }
//                 }

//                 methodCommitMap.put(m, finalList);
                
//                 logger.debug("{} - {}", m.asString(), commits.size());
//             }
//         }

//         git.reset().setMode(ResetType.HARD).call();
//         git.checkout().setName("master").call();

//         return this;
//     }

//     public Experiment1 setOutputDir(String outputDir) {
//         this.outputDir = outputDir;
//         return this;
//     }

//     public Experiment1 report() {
//         String[] pathSplit = projectPath.split("/");

//         File outputDirFile = new File(this.outputDir);
//         if (!outputDirFile.exists()) {
//             outputDirFile.mkdirs();
//         }

//         String outputFileName = String.format("%s-%s.json", pathSplit[pathSplit.length - 1], this.commitID);

//         File report = new File(
//             outputDirFile,
//             outputFileName
//         );

//         JsonObject content = new JsonObject();
        
//         for (Entry<String, String> entry : properties.entrySet()) {
//             String key = entry.getKey();
//             String value = entry.getValue();

//             content.addProperty(key, value);
//         }

//         JsonArray methods = new JsonArray();
//         for (Entry<MethodSignature, List<Pair<String, String>>> entry : methodCommitMap.entrySet()) {
//             MethodSignature m = entry.getKey();
//             List<Pair<String, String>> commits = entry.getValue();
//             JsonElement mJson = gson.toJsonTree(m);
//             JsonArray commitsJson = new JsonArray();

//             for (Pair<String, String> commit: commits) {
//                 JsonObject commitJson = new JsonObject();
//                 commitJson.addProperty("SHA1", commit.getLeft());
//                 commitJson.addProperty("date", commit.getRight());
//                 commitsJson.add(commitJson);
//             }
            
//             mJson.getAsJsonObject().add("commits", commitsJson);

//             methods.add(mJson);
//         }
//         content.add("methods", methods);

//         try {
//             FileWriter writer = new FileWriter(report);
//             gson.toJson(content, writer);
//             writer.flush();
//             writer.close();
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//         return this;
//     }

//     public static void main(String[] args) throws ParseException {
//         Options options = new Options();

//         options.addOption("s", "sut", true, "Path to the system under study.");
//         options.addOption("c", "commit", true, "Commit from which the experiment starts.");
//         options.addOption("o", "output", true, "Output directory");

//         CommandLineParser parser = new DefaultParser();
//         CommandLine cmd = parser.parse(options, args);

//         // Initialize the experiment and set all the parameters
//         Experiment1 Experiment1 = new Experiment1();

//         if (cmd.hasOption("sut") && cmd.hasOption("commit") && cmd.hasOption("output")) {
//             Experiment1.setProject(cmd.getOptionValue("sut"), cmd.getOptionValue("commit"));
//             Experiment1.setOutputDir(cmd.getOptionValue("output"));
//         }
//         else {
//             HelpFormatter formatter = new HelpFormatter();
//             formatter.printHelp("CLITester", options);
//             System.exit(1);
//         }

//         // Run the experiment
//         try{
//             Experiment1.run()
//                 .report();
//         } catch (Exception e) {
//             e.printStackTrace();
//             System.exit(1);
//         }
//     }
// }