package de.visone.crawl.out;

import java.io.Closeable;

/**
 * This listener gets notified whenever a page has finished being crawled.
 * 
 * @author Joschi
 * 
 */
public interface CrawlListener extends Closeable {

	/**
	 * This method is called whenever a page was crawled.
	 * 
	 * @param c
	 *            The crawled content of the page. Note that this Object may not
	 *            yield correct results after this method has returned.
	 */
	void pageCrawled(Content c);

}
