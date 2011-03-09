package de.visone.crawl.sys;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.visone.crawl.accept.LinkAccepter;
import de.visone.crawl.rules.BlacklistFilter;
import de.visone.crawl.rules.RuleManager;
import de.visone.crawl.texter.TexterFactory;

public abstract class AbstractUrlPool {

	private static final Random rnd = new Random();

	private final List<LinkAccepter> accepter;

	private final long meanDelay;

	private final int killLimit;

	private final Map<String, BlacklistFilter> filter;

	private final int maxRetries;

	private int soFar;

	protected final TexterFactory texter;

	public AbstractUrlPool(final TexterFactory texter, final long meanDelay,
			final int killLimit, final int maxRetries) {
		this.meanDelay = meanDelay / 2;
		this.killLimit = killLimit;
		this.texter = texter;
		this.maxRetries = maxRetries;
		filter = new HashMap<String, BlacklistFilter>();
		accepter = new ArrayList<LinkAccepter>();
		soFar = 0;
	}

	public void addAccepter(final LinkAccepter acc) {
		if (acc == null) {
			return;
		}
		accepter.add(acc);
	}

	public boolean append(final URL url, final CrawlState state) {
		return append(url, state, false);
	}

	public boolean append(final URL url, final CrawlState state,
			final boolean dry) {
		if (url == null) {
			return false;
		}
		if (texter.isDomainSpecific()) {
			final String rule = RuleManager.getRuleForURL(url);
			if (rule != null && !filter.containsKey(rule)) {
				RuleManager.addDomainSpecific(rule, filter,
						texter.getDSQueries());
				addAccepter(filter.get(rule));
			}
		}
		if (state == null) {
			final CrawlState start = new CrawlState(url, statelessDepth(),
					texter.getInstance(url));
			if (!dry) {
				add(start, null);
			}
			return true;
		}
		final CrawlState link = new CrawlState(url, state.getDepth() + 1,
				texter.getInstance(url));
		if (acceptedNotAdded(link, state)) {
			return true;
		}
		for (final LinkAccepter acc : accepter) {
			if (!acc.accept(url, state)) {
				return false;
			}
		}
		if (!dry) {
			add(link, state);
		}
		return true;
	}

	public CrawlState getNext() throws InterruptedException {
		if (hasReachedKillLimit()) {
			return null;
		}
		final CrawlState res = getNextUrl();
		++soFar;
		return res;
	}

	/**
	 * @param link
	 *            The link.
	 * @param parent
	 *            The parent of this link or <code>null</code>.
	 * @return <code>true</code> if the link was accepted but (e.g. because it
	 *         is already in the list) it was not added to the queue.
	 *         <code>false</code> if the link was only accepted.
	 * 
	 * @see #add(CrawlState, CrawlState)
	 */
	protected abstract boolean acceptedNotAdded(CrawlState link,
			CrawlState parent);

	/**
	 * @return The next URL in the Queue. This method should block (via
	 *         {@link #delayFor(CrawlState)}) until the next item is allowed to
	 *         be crawled.
	 * @throws InterruptedException
	 *             Thrown by {@link #delayFor(CrawlState)}.
	 */
	protected abstract CrawlState getNextUrl() throws InterruptedException;

	/**
	 * Adds a link to the queue.
	 * 
	 * @param link
	 *            The link.
	 * @param parent
	 *            The parent of this link. This is not always the only parent
	 *            since many pages can point to this particular URL. It may also
	 *            be <code>null</code>.
	 */
	protected abstract void add(CrawlState link, CrawlState parent);

	/**
	 * Adds a previously added state back to the list if it is allowed by the
	 * settings. Otherwise the state gets disposed.
	 * 
	 * @param link
	 *            The link.
	 */
	public void addAgain(final CrawlState link) {
		if (link.mayTryAgain(maxRetries)) {
			link.tryAgain();
			add(link, link.getParent());
		} else {
			link.dispose();
		}
	}

	/**
	 * @return Whether there is a next URL to crawl.
	 */
	protected abstract boolean hasNextUrl();

	/**
	 * @return Defines the link depth for initial URLs.
	 */
	protected abstract int statelessDepth();

	/**
	 * @param level
	 *            The link depth.
	 * @return Gives the progress for the given level.
	 */
	public abstract double getProgress(final int level);

	private boolean hasReachedKillLimit() {
		return killLimit > 0 && soFar >= killLimit;
	}

	public boolean hasNext() {
		return !hasReachedKillLimit() && hasNextUrl();
	}

	protected void delayFor(final CrawlState res) throws InterruptedException {
		if (res.getDepth() <= 0) {
			return;
		}
		synchronized (this) {
			final long rtime = meanDelay
					+ (long) (rnd.nextDouble() * meanDelay);
			System.err.println("wait " + rtime + "ms");
			if (rtime > 0) {
				wait(rtime);
			}
		}
	}

}
