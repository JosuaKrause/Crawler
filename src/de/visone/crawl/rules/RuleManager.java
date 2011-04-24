package de.visone.crawl.rules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class RuleManager {

	private static File BASE = new File(".");

	public static void setBaseDir(final File base) {
		BASE = base;
	}

	public static final String EXT = ".rule";

	public static String getFullRuleForURL(final URL url) {
		return createRuleFromURL(url) + EXT;
	}

	private static String createRuleFromURL(final URL url) {
		final String path = url.getPath();
		if (path.isEmpty()) {
			return url.getHost();
		}
		final String[] seg = path.split("/");
		final StringBuilder sb = new StringBuilder();
		int i = seg.length;
		while (--i >= 0) {
			if (seg[i].isEmpty()) {
				continue;
			}
			sb.append(seg[i]);
			sb.append('.');
		}
		sb.append(url.getHost());
		return sb.toString();
	}

	public static String getRuleForURL(final URL url) {
		String rule = getFullRuleForURL(url);
		File file;
		do {
			file = new File(BASE, rule);
			rule = rule.substring(rule.indexOf('.') + 1);
		} while (!file.exists() && rule.length() > EXT.length());
		return file.exists() ? file.getName() : maybeDefaultRule();
	}

	private static String maybeDefaultRule() {
		final File defaultRule = new File(BASE, EXT);
		return defaultRule.exists() ? defaultRule.getName() : null;
	}

	public static String[] getRules() {
		return BASE.list(new FilenameFilter() {

			@Override
			public boolean accept(final File dir, final String name) {
				return name.endsWith(EXT);
			}
		});
	}

	public static File getFileFromRule(final String rule) {
		return new File(BASE, rule);
	}

	public static void writeRules(final File dest, final List<String> blRules,
			final List<String> textRules) throws IOException {
		final PrintStream ps = new PrintStream(new FileOutputStream(dest),
				true, "UTF-8");
		ps.println(BLACKLIST_SECTION);
		for (final String r : blRules) {
			ps.println(r);
		}
		ps.println();
		ps.println(TEXT_SECTION);
		for (final String r : textRules) {
			ps.println(r);
		}
		ps.println();
		ps.close();
	}

	public static void addRule(final String ruleFile,
			final BlacklistFilter filter, final HtmlQuery treeRules) {
		final File file = new File(BASE, ruleFile);
		if (!file.exists()) {
			return;
		}
		try {
			new RuleManager(file).parse(filter, treeRules);
		} catch (final IOException e) {
			// nothing to do -- should never happen
		}
	}

	public static void addDomainSpecific(String rule,
			final Map<String, BlacklistFilter> filter,
			final Map<String, HtmlQuery> treeRules) {
		File file;
		final Set<String> oldRules = new HashSet<String>();
		do {
			if (filter.containsKey(rule)) {
				return;
			}
			oldRules.add(rule);
			file = new File(BASE, rule);
			rule = rule.substring(rule.indexOf('.') + 1);
		} while (!file.exists() && rule.length() > EXT.length());
		if (!file.exists()) {
			for (final String h : oldRules) {
				filter.put(h, null);
			}
			return;
		}
		try {
			final BlacklistFilter f = new BlacklistFilter(oldRules);
			final HtmlQuery l = new HtmlQuery(file.getName());
			new RuleManager(file).parse(f, l);
			for (final String h : oldRules) {
				filter.put(h, f);
				treeRules.put(h, l);
			}
		} catch (final IOException e) {
			// nothing to do -- should never happen
		}
	}

	public static void addQuery(final List<String> rules, final HtmlQuery q) {
		new RuleManager(rules.iterator()).parseQuery(q);
	}

	public static void addBlacklist(final List<String> rules,
			final BlacklistFilter b) {
		new RuleManager(rules.iterator()).parseBlackList(b);
	}

	public static final String BLACKLIST_SECTION = "[blacklist]";

	public static final String TEXT_SECTION = "[queries]";

	private final Scanner scan;

	private final Iterator<String> it;

	private RuleManager(final File file) throws FileNotFoundException {
		scan = new Scanner(file, "UTF-8");
		it = null;
	}

	private RuleManager(final Iterator<String> it) {
		this.it = it;
		scan = null;
	}

	private boolean hasNextLine() {
		return it != null ? it.hasNext() : scan.hasNextLine();
	}

	private String nextLine() {
		if (it != null) {
			if (it.hasNext()) {
				return it.next();
			}
			return null;
		}
		while (scan.hasNextLine()) {
			final String s = scan.nextLine().trim();
			if (!s.isEmpty() && !s.startsWith("#")) {
				return s;
			}
		}
		return null;
	}

	public void parse(final BlacklistFilter filter, final HtmlQuery treeRules) {
		String s = null;
		while (hasNextLine()) {
			if (s == null) {
				s = nextLine();
			}
			if (s == null) {
				break;
			}
			if (s.equals(BLACKLIST_SECTION)) {
				s = parseBlackList(filter);
			} else if (s.equals(TEXT_SECTION)) {
				s = parseQuery(treeRules);
			} else {
				s = null;
			}
		}
		scan.close();
	}

	private String parseQuery(final HtmlQuery q) {
		while (hasNextLine()) {
			final String s = nextLine();
			if (s == null || s.startsWith("[")) {
				return s;
			}
			if (q == null) {
				continue;
			}
			final String[] all = s.split(" ");
			int step = 0;
			int sign = 0;
			String name = null;
			final List<String> rules = new LinkedList<String>();
			for (final String r : all) {
				if (r.isEmpty()) {
					continue;
				}
				if (step == 0) {
					if (r.equals("+")) {
						sign = 1;
					} else if (r.equals("-")) {
						sign = -1;
					}
					++step;
				} else if (step == 1) {
					name = r;
					++step;
				} else {
					rules.add(r);
				}
			}
			if (sign > 0) {
				q.addInclude(name, rules.toArray());
			} else if (sign < 0) {
				q.addExclude(name, rules.toArray());
			}
		}
		return null;
	}

	private String parseBlackList(final BlacklistFilter filter) {
		while (hasNextLine()) {
			final String s = nextLine();
			if (s == null || s.startsWith("[")) {
				return s;
			}
			if (filter == null) {
				continue;
			}
			filter.addRule(s);
		}
		return null;
	}

}
