package org.spideruci.history.slicer.slicers;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistorySlicerBuilder {

	private boolean backwards;
	private Logger logger = LoggerFactory.getLogger(HistorySlicerBuilder.class);
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

	@API(status = Status.EXPERIMENTAL)
	public HistorySlicerBuilder setForwardSlicing(boolean forward) {
		this.backwards = !forward;
		return this;
	}

	public HistorySlicer build(Repository repo) {
		if (!this.backwards) {
			logger.error("Forward Slicing is currently experimental.");
			return new ForwardHistorySlicer(repo);
		}
		return new BackwardHistorySlicer(repo);
	}

} 