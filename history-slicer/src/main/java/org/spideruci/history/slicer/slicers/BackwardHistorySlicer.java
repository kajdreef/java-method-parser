package org.spideruci.history.slicer.slicers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class BackwardHistorySlicer extends HistorySlicer {

	public BackwardHistorySlicer(Repository repo) {
		super(repo);
	}

	// TODO set range
	private List<RevCommit> traceCommits(String filePath) {
		List<RevCommit> result = new LinkedList<>();
		try {
			for (RevCommit commit : this.git.log().addPath(filePath).call()) {
				result.add(commit);
			}
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return result;

	}

	@Override
	public List<String> trace(String filePath) {
		List<String> result = new LinkedList<>();

		// Get all commits, and get the SHA of each commit
		traceCommits(filePath).stream().forEach(f -> result.add(f.getName()));

		return result;
	}

	@Override
	public List<String> trace(String filePath, int start_line, int end_line) {
		List<String> result = new LinkedList<>();

		String command = String.format(
			"git --no-pager log -L%d,%d:%s --oneline --no-patch",
			start_line,
			end_line,
			filePath
		);

		try {
			Process p = Runtime.getRuntime().exec(command, null, this.repo.getWorkTree());
			p.waitFor();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while(reader.ready()) {
				String line = reader.readLine();
				result.add(line.split(" ")[0]);
			}
		} catch (NoWorkTreeException | IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
}