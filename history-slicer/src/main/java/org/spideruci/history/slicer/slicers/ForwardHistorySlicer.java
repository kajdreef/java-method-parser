package org.spideruci.history.slicer.slicers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;

public class ForwardHistorySlicer extends HistorySlicer {

    public ForwardHistorySlicer(Repository repo) {
        super(repo);
    }

    // TODO set range
    @Override
    public List<String> trace(String filePath) {
        List<String> result = new LinkedList<>();

        String command = String.format("git rev-list HEAD {}", filePath);

        try {
            Process p = Runtime.getRuntime().exec(command, null, this.repo.getWorkTree());
            p.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            while (reader.ready()) {
                String line = reader.readLine();
                result.add(line.split(" ")[0]);
            }
        } catch (NoWorkTreeException | IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public List<String> trace(String filePath, int start_line, int end_line) {
        // TODO Auto-generated method stub
        return null;
    }
}