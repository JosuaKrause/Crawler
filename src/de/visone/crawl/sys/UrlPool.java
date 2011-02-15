package de.visone.crawl.sys;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import de.visone.crawl.Settings;
import de.visone.crawl.accept.LinkAccepter;
import de.visone.crawl.rules.BlacklistFilter;
import de.visone.crawl.rules.RuleManager;
import de.visone.crawl.texter.TexterFactory;

public class UrlPool {

	private static final Random rnd = new Random();

	private final Map<Integer, Integer> depth;

	private final Map<Integer, Integer> depthProg;

	private final TreeSet<CrawlState> urls;

	private final Set<CrawlState> done;

	private final List<LinkAccepter> accepter;

	private final long meanDelay;

	private final TexterFactory texter;

	private final Map<String, BlacklistFilter> filter;

	private final int killLimit;

	private int soFar;

	public UrlPool(final long meanDelay, final TexterFactory factory,
			final int killLimit) {
		this.meanDelay = meanDelay / 2;
		this.killLimit = killLimit;
		soFar = 0;
		texter = factory;
		urls = new TreeSet<CrawlState>();
		done = new HashSet<CrawlState>();
		accepter = new ArrayList<LinkAccepter>();
		depth = new HashMap<Integer, Integer>();
		depthProg = new HashMap<Integer, Integer>();
		filter = new HashMap<String, BlacklistFilter>();
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

	public void addAccepter(final LinkAccepter acc) {
		if (acc == null) {
			return;
		}
		accepter.add(acc);
	}

	private void add(final CrawlState link) {
		synchronized (urls) {
			urls.add(link);
			done.add(link);
			inc(link.getDepth());
		}
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
			final CrawlState start = new CrawlState(url,
					urls.isEmpty() ? 0 : 1, texter.getInstance(url));
			if (!dry) {
				add(start);
			}
			return true;
		}
		final CrawlState link = new CrawlState(url, state.getDepth() + 1,
				texter.getInstance(url));
		if (done.contains(link)) {
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
		if (killLimit > 0 && soFar >= killLimit) {
			return null;
		}
		final CrawlState res;
		synchronized (urls) {
			if (urls.isEmpty()) {
				return null;
			}
			res = urls.pollFirst();
			incProg(res.getDepth());
		}
		if (res.getDepth() > 0) {
			synchronized (this) {
				final long rtime = meanDelay
						+ (long) (rnd.nextDouble() * meanDelay);
				System.err.println("wait " + rtime + "ms");
				if (rtime > 0) {
					wait(rtime);
				}
			}
		}
		++soFar;
		return res;
	}

	public boolean hasNext() {
		return !urls.isEmpty() && (killLimit == 0 || soFar < killLimit);
	}

	public double getProgress(final int level) {
		final Integer all = depth.get(level);
		final Integer pro = depthProg.get(level);
		if (all == null || pro == null) {
			return 0.0;
		}
		return (double) pro / (double) all;
	}
}
