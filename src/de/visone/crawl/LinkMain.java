package de.visone.crawl;

import java.io.IOException;
import java.io.PrintStream;

import de.visone.crawl.gui.CrawlerDialog;
import de.visone.crawl.gui.LinkCrawler;
import de.visone.crawl.out.LinkWriter;
import de.visone.crawl.sys.Utils;

/**
 * Starts a GUI configured link crawler.
 * 
 * @author Joschi
 * 
 */
public final class LinkMain {

	/**
	 * Nothing to construct.
	 */
	private LinkMain() {
		// no constructor...
	}

	/**
	 * Starts a GUI configured link crawler. The destination file can be set via
	 * the arguments. If no arguments are given the output will be to the
	 * standard-out.
	 * 
	 * @param args
	 *            An optional file name for the output.
	 * @throws IOException
	 *             If an error occurs.
	 */
	public static void main(final String[] args) throws IOException {
		Utils.setLookAndFeel();
		final PrintStream ps = args.length == 0 ? System.out : new PrintStream(
				args[0], "UTF-8");
		final CrawlerDialog cd = new LinkCrawler(null);
		cd.setCrawlListener(new LinkWriter(ps));
		cd.packAndShow();
	}

}
