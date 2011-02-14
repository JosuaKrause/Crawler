package de.visone.crawl;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;

import de.visone.crawl.accept.LinkAccepter;
import de.visone.crawl.accept.MaxDepth;
import de.visone.crawl.accept.NoFollowAccepter;
import de.visone.crawl.accept.OnlySameHost;
import de.visone.crawl.out.CrawlListener;
import de.visone.crawl.sys.UrlPool;
import de.visone.crawl.sys.Utils;
import de.visone.crawl.texter.TexterFactory;
import de.visone.crawl.xml.CrawlerThread;
import de.visone.crawl.xml.ProgressProducer;

/**
 * This class sets up a crawler with a given configuration and starts it. The
 * current progress can also be obtained.
 * 
 * @see Settings
 * @see ProgressProducer
 * @see CrawlListener
 * 
 * @author Joschi
 * 
 */
public class Crawler {

	/**
	 * The thread crawling the net.
	 */
	private final CrawlerThread thread;

	/**
	 * Sets up the crawler and starts it. The progress can be obtained via
	 * {@link #getProgressProducer()}.
	 * 
	 * @param set
	 *            The settings of the crawler.
	 * @param start
	 *            The first URL to crawl.
	 * @param listener
	 *            The crawl listener to receive the results.
	 * @param followUp
	 *            An optional list of URLs to crawl. Usually it is empty and the
	 *            following URLs are taken from the <code>start</code>-Page.
	 */
	public Crawler(final Settings set, final String start,
			final CrawlListener listener, final Object... followUp) {
		final UrlPool pool = new UrlPool(set.meanDelay, new TexterFactory(set),
				set.killLimit);
		final URL ustart = Utils.getURL(start);
		pool.append(ustart, null);
		for (final Object url : followUp) {
			pool.append(Utils.getURL(url.toString()), null);
		}
		pool.addAccepter(new MaxDepth(set.maxDepth));
		if (set.onlySameHost) {
			pool.addAccepter(new OnlySameHost());
		}
		if (set.readNoFollow) {
			pool.addAccepter(new NoFollowAccepter());
		}
		if (set.acceptCookies) {
			final CookiePolicy cp = new CookiePolicy() {

				@Override
				public boolean shouldAccept(final URI uri,
						final HttpCookie cookie) {
					return uri.getHost().equals(ustart.getHost())
							&& HttpCookie.domainMatches(cookie.getDomain(), uri
									.getHost());
				}
			};
			CookieHandler.setDefault(new CookieManager(null, cp));
		}
		for (final LinkAccepter la : set.customAccepter) {
			pool.addAccepter(la);
		}
		if (set.authorizationName != null) {
			if (set.authorizationPassword == null) {
				set.authorizationPassword = "";
			}
			Utils.addCustomHeader("Authorization", "user "
					+ set.authorizationName + ":" + set.authorizationPassword);
		}
		thread = new CrawlerThread(pool, set, listener);
	}

	/**
	 * @return The progress producer of the running crawl.
	 */
	public ProgressProducer getProgressProducer() {
		return thread;
	}

	/**
	 * Starts the crawler. A crawler can only be started one time.
	 */
	public void start() {
		if (thread.isFresh()) {
			synchronized (thread) {
				if (thread.isFresh()) {
					thread.start();
				}
			}
		}
	}

}
