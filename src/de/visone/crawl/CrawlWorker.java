package de.visone.crawl;

import java.io.IOException;
import java.io.PrintStream;

import de.visone.crawl.out.Content;
import de.visone.crawl.out.CrawlListener;

public class CrawlWorker implements CrawlListener {

	public CrawlWorker(final PrintStream out) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return The Settings configuration for this crawler.
	 */
	public Settings getSettings() {
		final Settings s = new Settings();
		// TODO: configure settings
		return s;
	}

	@Override
	public void pageCrawled(final Content c) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}
}
