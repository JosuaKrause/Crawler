package de.visone.crawl.xml;

import java.io.IOException;

import org.xml.sax.SAXException;

import de.visone.crawl.Settings;
import de.visone.crawl.gui.CrawlerDialog;
import de.visone.crawl.out.CrawlListener;
import de.visone.crawl.sys.AbstractUrlPool;
import de.visone.crawl.sys.CrawlState;
import de.visone.crawl.sys.Utils;

public class CrawlerThread extends Thread implements ProgressProducer {

	private final String userAgent;

	private final AbstractUrlPool pool;

	private final int maxDepth;

	private final CrawlListener listener;

	private final boolean haltOnError;

	private final int coolDown;

	private final long forcedTimeout;

	private ProgressListener progress;

	private volatile int level;

	private volatile boolean hasFinished;

	private volatile Exception errorFlag;

	private String lastURL;

	private volatile boolean fresh = true;

	public CrawlerThread(final AbstractUrlPool pool, final Settings s,
			final CrawlListener listener) {
		super("Crawl-Thread");
		this.pool = pool;
		maxDepth = s.maxDepth;
		userAgent = s.userAgent;
		this.listener = listener;
		progress = null;
		hasFinished = false;
		haltOnError = s.haltOnError;
		coolDown = s.coolDown;
		forcedTimeout = s.forcedTimeoutAfter;
		errorFlag = null;
		lastURL = null;
	}

	private void finish() {
		final ProgressListener p = progress;
		progress = null;
		p.finished(lastURL, haltOnError ? errorFlag : null);
	}

	@Override
	public void setProgressListener(final ProgressListener p) {
		progress = p;
		if (progress != null) {
			if (hasFinished) {
				finish();
			} else {
				progress.progressAdvanced(getProgressLevel(), getProgress());
			}
		}
	}

	private void crawl(final CrawlState state) throws IOException, SAXException {
		level = state.getDepth();
		lastURL = state.getURL().toString();
		if (progress != null) {
			progress.progressAdvanced(getProgressLevel(), getProgress());
		}
		Utils.crawl(state, pool, userAgent);
	}

	private volatile boolean success = false;

	@Override
	public void run() {
		fresh = false;
		try {
			while (!isInterrupted() && pool.hasNext()) {
				final CrawlState state = pool.getNext();
				if (state != null) {
					success = false;
					final Thread crawl = new Thread() {
						@Override
						public void run() {
							try {
								crawl(state);
								listener.pageCrawled(state.getTexter());
								success = true;
							} catch (final Exception e) {
								errorFlag = e;
							}
						}
					};
					crawl.start();
					crawl.join(forcedTimeout);
					if (success) {
						state.dispose();
					} else {
						if (crawl.isAlive()) {
							crawl.interrupt();
							System.err.println("Forced Time-Out after "
									+ forcedTimeout + "ms!");
						}
						if (!haltOnError) {
							if (errorFlag != null) {
								errorFlag.printStackTrace();
							}
							if (coolDown > 0) {
								synchronized (this) {
									System.err.println("cooling down "
											+ coolDown + "ms");
									wait(coolDown);
								}
							}
						}
						pool.addAgain(state);
					}
				}
				if (haltOnError && errorFlag != null) {
					break;
				}
			}
		} catch (final InterruptedException e) {
			interrupt();
		}
		try {
			listener.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		hasFinished = true;
		if (progress != null) {
			finish();
		} else if (haltOnError && errorFlag != null && !isInterrupted()) {
			CrawlerDialog.printErrorDialog(lastURL, errorFlag, null);
		}
	}

	@Override
	public void cancelAction() {
		if (progress != null) {
			progress = null;
			interrupt();
		}
	}

	public double getProgressLevel() {
		return (double) level / (double) maxDepth;
	}

	public double getProgress() {
		return pool.getProgress(level);
	}

	@Override
	public boolean isCrawling() {
		return !hasFinished;
	}

	@Override
	public void waitFor() throws InterruptedException {
		join();
	}

	@Override
	public boolean isFresh() {
		return fresh;
	}

}
