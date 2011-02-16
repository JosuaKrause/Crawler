package de.visone.crawl.sys;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import de.visone.crawl.texter.Texter;
import de.visone.crawl.texter.TexterFactory;

public class DepthFstUrlPool extends AbstractUrlPool {

	private final Map<CrawlState, Texter> parentMap;

	private final Map<Texter, Queue<CrawlState>> urls;

	private Texter current;

	public DepthFstUrlPool(final TexterFactory texter, final long meanDelay,
			final int killLimit) {
		super(texter, meanDelay, killLimit);
		parentMap = new HashMap<CrawlState, Texter>();
		urls = new HashMap<Texter, Queue<CrawlState>>();
		current = null;
	}

	@Override
	protected boolean acceptedNotAdded(final CrawlState link) {
		return false;
	}

	@Override
	protected void add(final CrawlState link, final CrawlState parent) {
		final Texter t = link.getTexter();
		if (parent == null) {
			parentMap.put(link, link.getTexter());
		} else {
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
	}

	private void addHighLevel(final CrawlState link, final Texter parent) {
		final Queue<CrawlState> q = urls.get(parent);
		if (q == null) {
			throw new NullPointerException("q");
		}
		q.add(link);
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
		if (current == null) {
			return true;
		}
		final Queue<CrawlState> q = urls.get(current);
		if (q == null) {
			throw new NullPointerException("q");
		}
		if (q.isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	protected int statelessDepth() {
		return 0;
	}

}
