package de.visone.crawl.rules;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.visone.crawl.accept.LinkAccepter;
import de.visone.crawl.sys.CrawlState;
import de.visone.crawl.sys.Utils;

public class BlacklistFilter implements LinkAccepter {

	public static final String REGEXP = "regexp:";

	public static final String NREGEXP = "allow:";

	private final Set<URL> blackList;

	private final List<String> matches;

	private final Set<String> hosts;

	private final List<Pattern> patterns;

	private final List<Pattern> npatterns;

	private final Map<Pattern, String> patternNames;

	public BlacklistFilter(final Set<String> hosts) {
		this.hosts = hosts;
		blackList = new HashSet<URL>();
		matches = new LinkedList<String>();
		patterns = new LinkedList<Pattern>();
		npatterns = new LinkedList<Pattern>();
		patternNames = new HashMap<Pattern, String>();
	}

	public void addRule(String rule) {
		rule = rule.trim();
		if (rule.isEmpty()) {
			return;
		}
		if (rule.startsWith("http:")) {
			blackList.add(Utils.getURL(rule));
			return;
		}
		if (rule.startsWith(REGEXP)) {
			try {
				// TODO: experimental
				final Pattern p = Pattern.compile(rule.substring(REGEXP
						.length()));
				patterns.add(p);
				patternNames.put(p, rule);
			} catch (final PatternSyntaxException e) {
				// notify user about bad regexp...
				e.printStackTrace();
			}
			return;
		}
		if (rule.startsWith(NREGEXP)) {
			try {
				// TODO: experimental
				final Pattern p = Pattern.compile(rule.substring(NREGEXP
						.length()));
				npatterns.add(p);
				patternNames.put(p, rule);
			} catch (final PatternSyntaxException e) {
				// notify user about bad regexp...
				e.printStackTrace();
			}
			return;
		}
		matches.add(rule);
	}

	@Override
	public boolean accept(final URL url, final CrawlState state) {
		if (hosts != null
				&& !hosts.contains(RuleManager.getRuleForURL(state.getURL()))) {
			return true;
		}
		return getRejectRule(url) == null;
	}

	private String patternRule(final String u) {
		for (final Pattern p : npatterns) {
			if (p.matcher(u).matches()) {
				return null;
			}
		}
		for (final Pattern p : patterns) {
			if (p.matcher(u).matches()) {
				return patternNames.get(p);
			}
		}
		return null;
	}

	public String getRejectRule(final URL url) {
		if (blackList.contains(url)) {
			return url.toString();
		}
		final String u = url.toString();
		for (final String rule : matches) {
			final String[] strs = rule.split(" ");
			boolean match = true;
			for (final String s : strs) {
				if (s.isEmpty()) {
					continue;
				}
				if (!u.contains(s)) {
					match = false;
					break;
				}
			}
			if (match) {
				return rule;
			}
		}
		return patternRule(u);
	}

	public List<String> getRules() {
		final List<String> rules = new LinkedList<String>();
		for (final URL u : blackList) {
			rules.add(u.toString());
		}
		rules.addAll(matches);
		rules.addAll(patternNames.values());
		return rules;
	}

	public Set<String> getHosts() {
		return hosts;
	}

}
