package de.visone.crawl;

import java.io.IOException;

import de.visone.crawl.out.Content;
import de.visone.crawl.out.CrawlListener;

public class CrawlWorker implements CrawlListener {

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
