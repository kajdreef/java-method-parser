package org.spideruci.experiments;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
    private int epoch;
    private Logger logger = LoggerFactory.getLogger(Experiment1.class);

    public Experiment1() {

    }

    public Experiment1 setProject(String projectPath) {
        this.projectPath = projectPath;
        File repoDirectory = new File(projectPath + File.separator + ".git");
        try {
            this.repo = new FileRepositoryBuilder().setGitDir(repoDirectory).build();
        } catch (IOException e) {

        }

        return this;
    }

    public Experiment1 setEpoch(int epoch) {
        this.epoch = epoch;
        return this;
    }

    public void run() {
        logger.info("Experiment configurations: sut - {}, epoch - {}", this.projectPath, this.epoch);
        List<Component> list = new ParserLauncher().start(this.projectPath);

        HistorySlicer slicer = HistorySlicerBuilder.getInstance().setForwardSlicing(false).build(this.repo);

        for (Component c : list) {
            if (c instanceof MethodSignature) {
                MethodSignature m = (MethodSignature) c;
                slicer.trace(m.file_path, m.line_start, m.line_end);
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        Options options = new Options();

        options.addOption("s", "sut", true, "Path to the system under study.");
        options.addOption("e", "epoch", true, "Length of the epoch.");
        options.addOption("c", "start_commit", true, "Starting commit from which the experiment starts.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // Initialize the experiment and set all the parameters
        Experiment1 experiment1 = new Experiment1();

        if (cmd.hasOption("sut") ) {
            experiment1.setProject(cmd.getOptionValue("sut"));
        }
        else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CLITester", options);
            System.exit(1);
        }

        if (cmd.hasOption("epoch")) {
            experiment1.setEpoch(Integer.parseInt(cmd.getOptionValue("epoch")));
        }
        else {
            experiment1.setEpoch(365);
        }

        // Run the experiment
        experiment1.run();
    }
}