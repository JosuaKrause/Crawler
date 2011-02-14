package de.visone.crawl.google;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import de.visone.crawl.Crawler;
import de.visone.crawl.Settings;
import de.visone.crawl.out.CrawlListener;
import de.visone.crawl.rules.HtmlQuery;
import de.visone.crawl.sys.CSVReader;

public class GoogleResults {

	public static void main(final String[] args) throws IOException {
		if (args.length != 2) {
			System.err.println("Usage: <infile> <outfile>");
			return;
		}
		final File out = new File(args[1]);
		final Set<String> already = new HashSet<String>();
		if (out.exists()) {
			final CSVReader read = new CSVReader(out);
			while (read.hasNextRow()) {
				final String[] row = read.getNextRow();
				if (row.length > 1) {
					already.add(row[0]);
				}
			}
		}
		final ArrayList<String> urls = new ArrayList<String>();
		final Scanner s = new Scanner(new File(args[0]));
		while (s.hasNextLine()) {
			final String q = s.nextLine().trim();
			if (!already.contains(q)) {
				urls.add("http://www.google.com/search?q=" + q);
			} else {
				System.err.println(q + " already crawled");
			}
		}
		if (urls.isEmpty()) {
			System.err.println("Empty infile!");
			return;
		}
		final CrawlListener writer = new GoogleWriter(new FileOutputStream(out,
				true));
		final Settings set = new Settings();
		set.meanDelay = 1500;
		set.acceptCookies = false;
		set.doLinks = false;
		set.maxDepth = 0;
		set.domainSpecific = false;
		set.doText = true;
		set.haltOnError = true;
		set.xmlText = false;
		set.query = new HtmlQuery("googleQuery");
		set.query.addInclude("div", new String[] { "id=resultStats" });
		set.query.addExclude("nobr");
		new Crawler(set, urls.get(0), writer, urls.subList(1, urls.size())
				.toArray()).start();
	}

}
