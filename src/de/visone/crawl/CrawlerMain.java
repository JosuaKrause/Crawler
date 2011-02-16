package de.visone.crawl;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.visone.crawl.gui.CrawlerDialog;
import de.visone.crawl.gui.LinkCrawler;
import de.visone.crawl.rules.RuleManager;
import de.visone.crawl.sys.AbstractUrlPool;
import de.visone.crawl.sys.DepthFstUrlPool;
import de.visone.crawl.sys.Utils;
import de.visone.crawl.texter.TexterFactory;
import de.visone.crawl.xml.ProgressAdapter;

public class CrawlerMain {

	private static void usage() {
		System.err.println("Usage: [-gui] <ruledir>");
		System.err.println("-gui: opens a gui for crawling");
		System.err.println("<ruledir>: The directory with the rules");
		System.err.println("STD_IN: a list of URLs");
		System.err.println("STD_ERR: error informations");
		System.err.println("STD_OUT: an xml representation of the contents");
		System.exit(1);
	}

	private static String args[];

	private static void setArgs(final String[] a) {
		args = a;
	}

	private static String arg(final int i) {
		if (i < 0 || i >= args.length) {
			usage();
		}
		return args[i];
	}

	public static void main(final String[] args) {
		Utils.setLookAndFeel();
		setArgs(args);
		int i = 0;
		boolean gui = false;
		if (arg(i).equals("-gui")) {
			gui = true;
			++i;
		}
		final File ruledir = new File(arg(i++));
		try {
			final List<String> starts = new LinkedList<String>();
			fillStarts(starts);
			RuleManager.setBaseDir(ruledir);
			final CrawlWorker cl = new CrawlWorker();
			if (gui) {
				final String start = starts.get(0);
				final CrawlerDialog cd = new LinkCrawler(null,
						"Configure Crawler...", true, start);
				cd.setCrawlListener(cl);
				cd.packAndShow();
			} else {
				final Crawler c = new Crawler(cl.getSettings(), starts.get(0),
						cl, starts.size() > 1 ? starts
								.subList(1, starts.size()) : new String[0]) {
					@Override
					protected AbstractUrlPool createUrlPool(final Settings set) {
						return new DepthFstUrlPool(new TexterFactory(set),
								set.meanDelay, set.killLimit);
					}
				};
				c.getProgressProducer().setProgressListener(
						new ProgressAdapter() {
							@Override
							public void progressAdvanced(final double main,
									final double secondary) {
								System.err.println("Depth: " + main
										+ "% Progress: " + secondary + "%");
							}
						});
				c.start();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			usage();
		}
	}

	private static void fillStarts(final List<String> starts) {
		// TODO Auto-generated method stub

	}

}
