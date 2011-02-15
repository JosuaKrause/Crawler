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

	private int soFar;

	private final Map<String, BlacklistFilter> filter;

	protected final TexterFactory texter;

	public AbstractUrlPool(final TexterFactory texter, final long meanDelay,
			final int killLimit) {
		this.meanDelay = meanDelay / 2;
		this.killLimit = killLimit;
		this.texter = texter;
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
				RuleManager.addDomainSpecific(rule, filter, texter
						.getDSQueries());
				addAccepter(filter.get(rule));
			}
		}
		if (state == null) {
			final CrawlState start = new CrawlState(url, statelessDepth(),
					texter.getInstance(url));
			if (!dry) {
				add(start);
			}
			return true;
		}
		final CrawlState link = new CrawlState(url, state.getDepth() + 1,
				texter.getInstance(url));
		if (acceptedNotAdded(link)) {
			return true;
		}
		for (final LinkAccepter acc : accepter) {
			if (!acc.accept(url, state)) {
				return false;
			}
		}
		if (!dry) {
			add(link);
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

	protected abstract boolean acceptedNotAdded(CrawlState link);

	protected abstract CrawlState getNextUrl() throws InterruptedException;

	protected abstract void add(CrawlState link);

	protected abstract boolean hasNextUrl();

	protected abstract int statelessDepth();

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
