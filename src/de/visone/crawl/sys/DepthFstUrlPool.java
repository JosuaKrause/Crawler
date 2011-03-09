package de.visone.crawl.sys;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.visone.crawl.texter.Texter;
import de.visone.crawl.texter.TexterFactory;

public class DepthFstUrlPool extends AbstractUrlPool {

	private final Map<CrawlState, Texter> parentMap;

	private final Map<Texter, Queue<CrawlState>> urls;

	private final Map<Texter, Set<URL>> already;

	private Texter current;

	public DepthFstUrlPool(final TexterFactory texter, final long meanDelay,
			final int killLimit, final int maxRetries) {
		super(texter, meanDelay, killLimit, maxRetries);
		parentMap = new HashMap<CrawlState, Texter>();
		urls = new HashMap<Texter, Queue<CrawlState>>();
		already = new HashMap<Texter, Set<URL>>();
		current = null;
	}

	@Override
	protected boolean acceptedNotAdded(final CrawlState link,
			final CrawlState parent) {
		if (!parentMap.containsKey(parent)) {
			return false;
		}
		final Texter p = parentMap.get(parent);
		return already.get(p).contains(link.getURL());
	}

	@Override
	protected void add(final CrawlState link, final CrawlState parent) {
		// System.err.println("Link: " + link.getURL() + " Parent: "
		// + (parent != null ? parent.getURL() : "null"));
		final Texter t = link.getTexter();
		if (parent == null) {
			parentMap.put(link, link.getTexter());
		} else {
			link.setParent(parent);
			final Texter p = parentMap.get(parent);
			t.setParent(p);
			parentMap.put(link, p != null ? p : t);
		}
		if (t.getParent() == null) {
			addLowLevel(link);
		} else {
			addHighLevel(link, parentMap.get(link));
		}
	}

	private void addLowLevel(final CrawlState link) {
		final Queue<CrawlState> q = new LinkedList<CrawlState>();
		q.add(link);
		urls.put(link.getTexter(), q);
		final Set<URL> a = new HashSet<URL>();
		a.add(link.getURL());
		already.put(link.getTexter(), a);
	}

	private void addHighLevel(final CrawlState link, final Texter parent) {
		final Queue<CrawlState> q = urls.get(parent);
		if (q == null) {
			throw new NullPointerException("q");
		}
		q.add(link);
		final Set<URL> a = already.get(parent);
		a.add(link.getURL());
	}

	@Override
	protected CrawlState getNextUrl() throws InterruptedException {
		final CrawlState next = next(current);
		delayFor(next);
		return next;
	}

	private CrawlState next(final Texter t) {
		if (t == null) {
			current = urls.keySet().iterator().next();
			return next(current);
		}
		final Queue<CrawlState> q = urls.get(t);
		if (q == null) {
			throw new NullPointerException("q");
		}
		if (q.isEmpty()) {
			urls.remove(t);
			return next(null);
		}
		return q.poll();
	}

	@Override
	public double getProgress(final int level) {
		return urls.isEmpty() ? (current == null ? 0.0 : 1.0) : 1.0 / urls
				.size();
	}

	@Override
	protected boolean hasNextUrl() {
		if (urls.isEmpty()) {
			return false;
		}
		if (urls.size() > 1) {
			return true;
		}
		if (current == null) {
			return true;
		}
		final Queue<CrawlState> q = urls.get(current);
		if (q == null) {
			throw new NullPointerException("q");
		}
		return !q.isEmpty();
	}

	@Override
	protected int statelessDepth() {
		return 0;
	}

}
