package org.spideruci.history.slicer.slicers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spideruci.history.slicer.metrics.CodeChurn;
import org.spideruci.history.slicer.metrics.Metric;
import org.spideruci.history.slicer.metrics.TotalCommits;

public class BackwardHistorySlicer extends HistorySlicer {

	private Logger logger = LoggerFactory.getLogger(BackwardHistorySlicer.class);

	private List<Metric> metrics;

	public BackwardHistorySlicer(Repository repo) {
		super(repo);
		logger.info("Backwards history slicer.");

		this.metrics = new ArrayList<>();

		this.metrics.add(new CodeChurn());
		this.metrics.add(new TotalCommits());
	}

	private List<RevCommit> traceCommits(String filePath) {
		List<RevCommit> result = new LinkedList<>();
		try {
			LogCommand command = this.git.log().addPath(filePath);
			if (this.pastCommit != null && this.presentCommit != null) {
				command.addRange(this.pastCommit, this.presentCommit);
			}

			for (RevCommit commit : command.call()) {
				result.add(commit);
			}
		} catch (GitAPIException | IOException e) {
			e.printStackTrace();
		}
		return result;

	}

	@Override
	public Map<String, Object> trace(String filePath) {
		Map<String, Object> properties = new HashMap<>();
		List<String> commits = new LinkedList<>();

		// Get all commits, and get the SHA of each commit
		traceCommits(filePath).stream().forEach(f -> {
			commits.add(f.getName());
		});

		properties.put("commits", commits);

		return properties;
	}
	private String parseGitTimestampToString(int timestamp) {
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date(timestamp * 1000L));
	}

	@Override
	public Map<String, Object> trace(String filePath, int start_line, int end_line) {
		Map<String, Object> properties = new HashMap<>();

		try {
			String command = null;

			if (this.pastCommit == null || this.presentCommit == null) {

				command = String.format("git log -L%d,%d:%s --no-patch",
						start_line, end_line, filePath);
			}
			else {
				String pastDate;
				RevCommit past = repo.parseCommit(this.pastCommit);
				pastDate = parseGitTimestampToString(past.getCommitTime());

				RevCommit present = repo.parseCommit(this.presentCommit);

				command = String.format(
						"git log -L%d,%d:%s --no-patch --after=\'%s\' %s",
						start_line, end_line, filePath, pastDate, present.getId().getName());
			}

			assert command != null;

			Process p = Runtime.getRuntime().exec(command, null, this.repo.getWorkTree());
			p.waitFor();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			StringBuilder sb = new StringBuilder();
			String line;
			List<String> diffs = new LinkedList<>();
			
			while((line = reader.readLine()) != null) {
				
				if (line.startsWith("commit ")) {
					if (sb.length() > 0) {
						diffs.add(sb.toString());
					}
					
					// Start the next diff
					sb = new StringBuilder();
				}

				sb.append(line).append("\n");
			}
			if (sb.length() > 0) {
				diffs.add(sb.toString());
			}

			for (Metric metric : this.metrics) {
				logger.debug("{} - {}", metric.getMetricType(), metric.compute(diffs));
				properties.put(metric.getMetricType(), metric.compute(diffs));
			}

			properties.put("commits", getCommitHashes(diffs));

			return properties;

		} catch (NoWorkTreeException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		return new HashMap<>();
	}

	private List<String> getCommitHashes(List<String> diffs) {
		List<String> results = new LinkedList<>();

		for (String diff: diffs) {
			String commit_line = diff.split("\n")[0];
			results.add(commit_line.split(" ")[1]);
		}
		return results;
	}
}