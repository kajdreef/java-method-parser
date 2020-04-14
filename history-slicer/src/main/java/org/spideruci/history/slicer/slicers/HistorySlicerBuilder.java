package org.spideruci.history.slicer.slicers;

import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistorySlicerBuilder {

	private Logger logger = LoggerFactory.getLogger(HistorySlicerBuilder.class);
	private static HistorySlicerBuilder builder = null;

	private HistorySlicerBuilder() {
	}

	public static HistorySlicerBuilder getInstance() {
		if (builder == null) {
			builder = new HistorySlicerBuilder();
		}
		return builder;
	}

	public HistorySlicer build(Repository repo) {
		logger.info("Initialized a backwards history slicer...");
		return new BackwardHistorySlicer(repo);
	}

} 