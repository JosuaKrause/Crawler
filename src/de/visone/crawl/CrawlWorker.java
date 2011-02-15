package de.visone.crawl;

import de.visone.crawl.out.CrawlListener;

public interface CrawlWorker extends CrawlListener {

	/**
	 * @return The Settings configuration for this crawler.
	 */
	Settings getSettings();
}
