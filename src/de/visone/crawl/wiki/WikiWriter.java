package de.visone.crawl.wiki;

import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import javax.swing.JOptionPane;

import de.visone.crawl.out.Content;
import de.visone.crawl.out.LinkWriter;
import de.visone.crawl.sys.Link;

public class WikiWriter extends LinkWriter {

	private final Component parent;

	private final Map<String, Boolean> sectionMap;

	private final PrintStream decisionWriter;

	public WikiWriter(final PrintStream out, final Component parent,
			final String decisions) throws IOException {
		super(out);
		this.parent = parent;
		sectionMap = new HashMap<String, Boolean>();
		final File dOut = new File(decisions);
		try {
			fillSectionMap(new Scanner(dOut));
		} catch (final FileNotFoundException e) {
			// do nothing...
		}
		decisionWriter = new PrintStream(new BufferedOutputStream(
				new FileOutputStream(dOut, true)), true, "UTF-8");
	}

	private void fillSectionMap(final Scanner s) {
		while (s.hasNextLine()) {
			final String line = s.nextLine().trim();
			final String name = line.substring(1);
			if (line.isEmpty()) {
				continue;
			}
			switch (line.charAt(0)) {
			case '+':
				sectionMap.put(name, true);
				break;
			case '-':
				sectionMap.put(name, false);
				break;
			case '#': // comment
			default: // ignore default
			}
		}
		s.close();
	}

	private boolean takeSection(final String name, final String url) {
		// normalized name
		final int p = predecide(name);
		if (p != 0) {
			return p > 0;
		}
		if (!sectionMap.containsKey(name)) {
			final int value = JOptionPane.showConfirmDialog(parent,
					"<html>Should the section '" + name + "'<br>on the page '"
							+ url + "' be accepted?", "Section: " + name,
					JOptionPane.YES_NO_OPTION);
			final boolean res = value == JOptionPane.YES_OPTION;
			sectionMap.put(name, res);
			System.out.println("SECTION: " + name + " -> " + res);
			decisionWriter.println((res ? "+" : "-") + name);
		}
		return sectionMap.get(name);
	}

	private int predecide(final String name) {
		// normalized name
		// negative
		if (name.contains("bibliography")) {
			return -1;
		}
		if (name.contains("early life")) {
			return -1;
		}
		if (name.contains("reference")) {
			return -1;
		}
		if (name.contains("external link")) {
			return -1;
		}
		if (name.contains("biography")) {
			return -1;
		}
		if (name.contains("source")) {
			return -1;
		}
		if (name.contains("history")) {
			return -1;
		}
		if (name.contains("career")) {
			return -1;
		}
		if (name.contains("see also")) {
			return -1;
		}
		if (name.contains("list")) {
			return -1;
		}
		if (name.contains("non-fiction")) {
			return -1;
		}
		// positive
		if (name.contains("criticism")) {
			return 1;
		}
		if (name.contains("theme")) {
			return 1;
		}
		return 0;
	}

	private String normalize(String s) {
		s = s.toLowerCase();
		// simple s stemming
		if (s.charAt(s.length() - 1) == 's') {
			return s.substring(0, s.length() - 1);
		}
		return s;
	}

	public String interpretString(final Scanner s, final String url) {
		System.out.println("URL: " + url);
		final StringBuilder sb = new StringBuilder();
		Queue<String> toc = null;
		boolean takeAnything = true;
		boolean analyzeToc = false;
		while (s.hasNextLine()) {
			final String line = s.nextLine().trim();
			if (line.isEmpty()) {
				continue;
			}
			if (toc == null) {
				if (line.equals("Contents")) { // found toc
					toc = new LinkedList<String>();
					analyzeToc = true;
				} else {
					sb.append(line + '\n'); // append everything before the toc
				}
				continue;
			}
			if (analyzeToc) {
				if (isNum(line.charAt(0))) {
					final String t = getTocEntry(normalize(line));
					if (t != null) {
						toc.add(t);
					}
					continue;
				}
				analyzeToc = false;
			}
			// System.out.println("LINE: " + line);
			if (normalize(line).equals(toc.peek())) { // found section title
				// toc.peek() may be null
				takeAnything = takeSection(toc.poll(), url);
				continue;
			}
			if (takeAnything) { // append the current section
				sb.append(line + '\n');
			}
		}
		s.close();
		return sb.toString();
	}

	private boolean isNum(final char c) {
		return (c >= '0' && c <= '9');
	}

	private String getTocEntry(final String toc) {
		final int len = toc.length();
		final char[] str = toc.toCharArray();
		int start = 1; // we checked the first character already
		while (isNum(str[start]) && start < len) {
			++start;
		}
		if (str[start] == '.') {
			return null;
		}
		return toc.substring(start).trim();
	}

	@Override
	public void pageCrawled(final Content c) {
		super.pageCrawled(new WikiContent(c));
	}

	private class WikiContent implements Content {

		private final URL url;

		private final Link[] accepted;

		private final Link[] other;

		private final String str;

		public WikiContent(final Content c) {
			accepted = new Link[0]; // c.getAcceptedLinks();
			other = new Link[0]; // c.getOtherLinks();
			url = c.getURL();
			str = interpretString(new Scanner(c.getText()), url.toString());
		}

		@Override
		public Link[] getAcceptedLinks() {
			return accepted;
		}

		@Override
		public Link[] getOtherLinks() {
			return other;
		}

		@Override
		public URL getURL() {
			return url;
		}

		@Override
		public String getText() {
			return str;
		}

	}

	@Override
	public void close() throws IOException {
		super.close();
		decisionWriter.close();
	}
}
