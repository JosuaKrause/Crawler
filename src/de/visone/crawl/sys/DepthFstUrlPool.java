package de.visone.crawl.sys;

import de.visone.crawl.texter.TexterFactory;

public class DepthFstUrlPool extends AbstractUrlPool {

	public DepthFstUrlPool(final TexterFactory texter, final long meanDelay,
			final int killLimit) {
		super(texter, meanDelay, killLimit);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean acceptedNotAdded(final CrawlState link) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void add(final CrawlState link, final CrawlState parent) {
		// TODO Auto-generated method stub

	}

	@Override
	protected CrawlState getNextUrl() throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProgress(final int level) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected boolean hasNextUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected int statelessDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

}
