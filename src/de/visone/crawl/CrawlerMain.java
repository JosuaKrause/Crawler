package de.visone.crawl;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import de.visone.crawl.gui.CrawlerDialog;
import de.visone.crawl.gui.LinkCrawler;
import de.visone.crawl.rules.RuleManager;
import de.visone.crawl.sys.Utils;
import de.visone.crawl.xml.ProgressAdapter;

public class CrawlerMain {

	private static String START = "start.txt";

	private static void usage() {
		System.err.println("Usage: [-gui] <ruledir> <listener>");
		System.err.println("-gui: opens a gui for crawling");
		System.err.println("<ruledir>: The directory with the rules");
		System.err.println("    it should also contain a file " + START);
		System.err.println("    containing the start URL(s)");
		System.err.println("<listener>: The " + CrawlWorker.class.getName()
				+ " implementing");
		System.err.println("    listener");
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
		final String listener = arg(i++);
		try {
			final Scanner s = new Scanner(new File(ruledir, START));
			final List<String> starts = new LinkedList<String>();
			while (s.hasNextLine()) {
				starts.add(s.nextLine().trim());
			}
			if (starts.isEmpty()) {
				System.err.println(START + " is empty");
				usage();
			}
			RuleManager.setBaseDir(ruledir);
			final CrawlWorker cl = getCrawlWorker(listener);
			if (gui) {
				final String start = starts.get(0);
				final CrawlerDialog cd = new LinkCrawler(null,
						"Configure Crawler...", true, start);
				cd.setCrawlListener(cl);
				cd.packAndShow();
			} else {
				final Crawler c = new Crawler(cl.getSettings(), starts.get(0),
						cl, starts.subList(1, starts.size()));
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

	private static CrawlWorker getCrawlWorker(final String className)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		final Class<?> c = Class.forName(className);
		final Object o = c.newInstance();
		if (!(o instanceof CrawlWorker)) {
			System.err.println(className + " is not a "
					+ CrawlWorker.class.getName());
			usage();
		}
		return (CrawlWorker) o;
	}
}
