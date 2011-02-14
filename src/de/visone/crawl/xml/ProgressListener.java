package de.visone.crawl.xml;

/**
 * A progress listener gets notified about the overall crawling progress.
 * 
 * @author Joschi
 * 
 */
public interface ProgressListener {

	/**
	 * Whenever a page is crawled this method is called.
	 * 
	 * @param main
	 *            The ratio of the link depth.
	 * @param secondary
	 *            The ratio of the pages on this level.
	 */
	void progressAdvanced(double main, double secondary);

	/**
	 * When the overall crawling process has finished this method is called.
	 * 
	 * @param url
	 *            The last URL crawled.
	 * @param err
	 *            When an exception occurred and stopped the process this
	 *            attribute is set non null.
	 */
	void finished(String url, Exception err);

}
