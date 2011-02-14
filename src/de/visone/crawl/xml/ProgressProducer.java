package de.visone.crawl.xml;

/**
 * The progress producer holds information about the state of the crawler.
 * 
 * @author Joschi
 * 
 */
public interface ProgressProducer {

	/**
	 * @return Whether the crawler is still crawling.
	 */
	boolean isCrawling();

	/**
	 * @return <code>true</code> before the crawler has been started.
	 */
	boolean isFresh();

	/**
	 * Sets the progress listener which is notified about the progress.
	 * 
	 * @param p
	 *            The listener.
	 */
	void setProgressListener(final ProgressListener p);

	/**
	 * Cancels the current crawling progress. A canceled crawler can not be
	 * restarted.
	 */
	void cancelAction();

	/**
	 * Blocks until the crawler has finished.
	 * 
	 * @throws InterruptedException
	 *             When the waiting period was interrupted.
	 */
	void waitFor() throws InterruptedException;

}
