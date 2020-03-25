import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.spideruci.history.slicer.slicers.HistorySlicer;
import org.spideruci.history.slicer.slicers.HistorySlicerBuilder;
import org.spideruci.line.extractor.ParserLauncher;
import org.spideruci.line.extractor.parsers.components.Component;
import org.spideruci.line.extractor.parsers.components.MethodSignature;

public class Experiment1 {

    private String projectPath;
    private Repository repo;

    public Experiment1 () {

    }

    public Experiment1 setProject(String projectPath) {
        this.projectPath = projectPath;
        File repoDirectory = new File(projectPath + File.separator + ".git");
        try{
            this.repo = new FileRepositoryBuilder().setGitDir(repoDirectory).build();
        }catch (IOException e) {

        }
        
        return this;
    }

    public void run() {
        List<Component> list = new ParserLauncher()
            .start(this.projectPath);
        
        HistorySlicer slicer = HistorySlicerBuilder.getInstance()
            .setForwardSlicing(false)
            .build(this.repo);

        for (Component c : list ){
            if (c instanceof MethodSignature) {
                MethodSignature m = (MethodSignature) c;
                slicer.trace(m.file_path, m.line_start, m.line_end);
            }
        }
    }

    public static void main(String[] args) {
        String projectPath = args[0];

        new Experiment1()
            .setProject(projectPath)
            .run();
    }
}