package de.visone.crawl;

import java.io.IOException;
import java.io.PrintStream;

import de.visone.crawl.gui.CrawlerDialog;
import de.visone.crawl.gui.TextCrawler;
import de.visone.crawl.out.TextWriter;
import de.visone.crawl.sys.Utils;

/**
 * Starts a GUI configured text crawler.
 * 
 * @author Joschi
 * 
 */
public class TextMain {

	/**
	 * Nothing to construct.
	 */
	private TextMain() {
		// no constructor...
	}

	/**
	 * Starts a GUI configured text crawler. The destination file can be set via
	 * the arguments. If no arguments are given the output will be to the
	 * standard-out.
	 * 
	 * @param args
	 *            An optional file name for the output.
	 * @throws IOException
	 *             If an error occures.
	 */
	public static void main(final String[] args) throws IOException {
		Utils.setLookAndFeel();
		final PrintStream ps = args.length == 0 ? System.out : new PrintStream(
				args[0], "UTF-8");
		final CrawlerDialog cd = new TextCrawler(null);
		cd.setCrawlListener(new TextWriter(ps));
		cd.packAndShow();
	}

}
