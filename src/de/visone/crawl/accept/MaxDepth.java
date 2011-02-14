package de.visone.crawl.accept;

import java.net.URL;

import de.visone.crawl.Settings;
import de.visone.crawl.sys.CrawlState;

/**
 * This {@link LinkAccepter} accepts only those URLs that are not found after a
 * certain depth has been reached. The usual way to use this accepter is to
 * modify the value of {@link Settings#maxDepth}.
 * 
 * @author Joschi
 * 
 */
public class MaxDepth implements LinkAccepter {

	/**
	 * The maximum crawl depth.
	 */
	private final int maxDepth;

	/**
	 * Creates an accepter with the given maximum depth.
	 * 
	 * @param maxDepth
	 *            The maximum depth.
	 */
	public MaxDepth(final int maxDepth) {
		this.maxDepth = maxDepth;
	}

	@Override
	public boolean accept(final URL url, final CrawlState state) {
		return state.getDepth() < maxDepth;
	}

}
