package de.visone.crawl.accept;

import java.net.URL;

import de.visone.crawl.Crawler;
import de.visone.crawl.Settings;
import de.visone.crawl.sys.CrawlState;

/**
 * A link accepter is called for every URL that was found on a page. It can
 * decide whether to follow that link or not. To add own link accepters one can
 * append them to the list ({@link Settings#customAccepter}) in the
 * {@link Settings} before creating a Crawler with it. Link accepters are only
 * asked when an URL is found on a page. They have no effects on the URLs
 * directly passed to the {@link Crawler}.
 * 
 * @see CrawlState
 * @see Crawler
 * @see Settings
 * 
 * @author Joschi
 * 
 */
public interface LinkAccepter {

	/**
	 * Determines whether to follow the given URL or not.
	 * 
	 * @param url
	 *            The URL in question.
	 * @param state
	 *            The current state of the crawler.
	 * @return <code>true</code> if the URL was accepted.
	 */
	boolean accept(URL url, CrawlState state);

}
