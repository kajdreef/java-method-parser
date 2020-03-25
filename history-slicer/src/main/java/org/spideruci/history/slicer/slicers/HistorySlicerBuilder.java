package org.spideruci.history.slicer.slicers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public class HistorySlicerBuilder {

	private boolean backwards;
	private static HistorySlicerBuilder builder = null;

	private HistorySlicerBuilder() {
		this.backwards = true;
	}

	public static HistorySlicerBuilder getInstance() {
		if (builder == null) {
			builder = new HistorySlicerBuilder();
		}
		return builder;
	}

	public HistorySlicerBuilder setForwardSlicing(boolean forward) {
		this.backwards = !forward;
		return this;
	}

	public HistorySlicer build(Repository repo) {
		if (this.backwards) {
			return new BackwardHistorySlicer(repo);
		} else {
			return new ForwardHistorySlicer(repo);
		}
	}

} 