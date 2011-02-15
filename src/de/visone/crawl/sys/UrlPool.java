package de.visone.crawl.sys;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.visone.crawl.Settings;
import de.visone.crawl.texter.TexterFactory;

public class UrlPool extends AbstractUrlPool {

	private final Map<Integer, Integer> depth;

	private final Map<Integer, Integer> depthProg;

	private final TreeSet<CrawlState> urls;

	private final Set<CrawlState> done;

	public UrlPool(final long meanDelay, final TexterFactory factory,
			final int killLimit) {
		super(factory, meanDelay, killLimit);
		urls = new TreeSet<CrawlState>();
		done = new HashSet<CrawlState>();
		depth = new HashMap<Integer, Integer>();
		depthProg = new HashMap<Integer, Integer>();
	}

	public UrlPool() {
		this(0, new TexterFactory(new Settings()), 0);
	}

	private void inc(final int d) {
		int old = 1;
		if (depth.containsKey(d)) {
			old += depth.get(d);
		}
		depth.put(d, old);
	}

	private void incProg(final int d) {
		int old = 1;
		if (depthProg.containsKey(d)) {
			old += depthProg.get(d);
		}
		depthProg.put(d, old);
	}

	@Override
	protected void add(final CrawlState link) {
		synchronized (urls) {
			urls.add(link);
			done.add(link);
			inc(link.getDepth());
		}
	}

	@Override
	protected CrawlState getNextUrl() throws InterruptedException {
		final CrawlState res;
		synchronized (urls) {
			if (urls.isEmpty()) {
				return null;
			}
			res = urls.pollFirst();
			incProg(res.getDepth());
		}
		delayFor(res);
		return res;
	}

	@Override
	protected boolean acceptedNotAdded(final CrawlState link) {
		return done.contains(link);
	}

	@Override
	protected boolean hasNextUrl() {
		return !urls.isEmpty();
	}

	@Override
	protected int statelessDepth() {
		return urls.isEmpty() ? 0 : 1;
	}

	@Override
	public double getProgress(final int level) {
		final Integer all = depth.get(level);
		final Integer pro = depthProg.get(level);
		if (all == null || pro == null) {
			return 0.0;
		}
		return (double) pro / (double) all;
	}
}
