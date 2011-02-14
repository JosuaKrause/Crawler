package de.visone.crawl.wiki;

import java.io.IOException;
import java.io.PrintStream;

import de.visone.crawl.gui.CrawlerDialog;
import de.visone.crawl.gui.LinkCrawler;

public class WikiResults {

	public static void main(final String[] args) throws IOException {
		final PrintStream ps = args.length == 0 ? System.out : new PrintStream(
				args[0], "UTF-8");
		final CrawlerDialog cd = new LinkCrawler(null, "Wiki crawler...", false);
		cd.setCrawlListener(new WikiWriter(ps, cd, "decisions.txt"));
		cd.packAndShow();
	}

}
