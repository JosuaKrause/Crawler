package de.visone.crawl.rules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.Attributes;

public class HtmlQuery {

	public static void addStd(final HtmlQuery query) {
		query.addExclude("script");
		query.addExclude("style");
		query.addInclude("body");
	}

	private static interface Query {

		boolean holdsFor(Attributes a);

		void addTo(List<String> list, String prefix);
	}

	private static class AndQuery implements Query {
		private final Map<String, String> atts;

		public AndQuery(final Object[] attsStrs) {
			atts = new HashMap<String, String>();
			for (final Object o : attsStrs) {
				final String[] r = o.toString().split("=", 2);
				if (r.length != 2) {
					continue;
				}
				if (r[1].startsWith("\"") && r[1].endsWith("\"")
						&& r[1].length() > 1) {
					r[1] = r[1].substring(1, r[1].length() - 1);
				}
				atts.put(r[0], r[1]);
			}
		}

		@Override
		public boolean holdsFor(final Attributes a) {
			for (final String n : atts.keySet()) {
				final int i = a.getIndex(n);
				if (i < 0) {
					return false;
				}
				final String ref = atts.get(n);
				if (ref == null) {
					return false;
				}
				final String v = a.getValue(i);
				if (v.equals(ref)) {
					continue;
				}
				if (!("^" + v + "$").contains(ref)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public void addTo(final List<String> list, final String prefix) {
			final StringBuilder sb = new StringBuilder(prefix);
			for (final Entry<String, String> e : atts.entrySet()) {
				sb.append(' ');
				sb.append(e.getKey());
				sb.append("=\"");
				sb.append(e.getValue());
				sb.append('"');
			}
			list.add(sb.toString());
		}
	}

	private static class OrQuery implements Query {
		private final Query first;
		private final Query second;

		public OrQuery(final Query first, final Query second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean holdsFor(final Attributes a) {
			if (first.holdsFor(a)) {
				return true;
			}
			return second.holdsFor(a);
		}

		@Override
		public void addTo(final List<String> list, final String prefix) {
			first.addTo(list, prefix);
			second.addTo(list, prefix);
		}
	}

	private static class WildCard implements Query {
		@Override
		public boolean holdsFor(final Attributes a) {
			return true;
		}

		@Override
		public void addTo(final List<String> list, final String prefix) {
			list.add(prefix);
		}
	}

	public Map<String, Query> include;

	public Map<String, Query> exclude;

	private final String originalName;

	public HtmlQuery(final String originalName) {
		this.originalName = originalName;
		include = new HashMap<String, Query>();
		exclude = new HashMap<String, Query>();
	}

	public void addInclude(final String name) {
		addInclude(name, new WildCard());
	}

	public void addInclude(final String name, final Object[] rules) {
		addInclude(name, new AndQuery(rules));
	}

	private void addInclude(final String name, final Query q) {
		final Query first = include.get(name);
		if (first != null) {
			include.put(name, new OrQuery(first, q));
		} else {
			include.put(name, q);
		}
	}

	public void addExclude(final String name) {
		addExclude(name, new WildCard());
	}

	public void addExclude(final String name, final Object[] rules) {
		addExclude(name, new AndQuery(rules));
	}

	private void addExclude(final String name, final Query q) {
		final Query first = exclude.get(name);
		if (first != null) {
			exclude.put(name, new OrQuery(first, q));
		} else {
			exclude.put(name, q);
		}
	}

	public int check(final String name, final Attributes a) {
		if (include.containsKey(name)) {
			final Query q = include.get(name);
			if (q.holdsFor(a)) {
				return 1;
			}
		}
		if (exclude.containsKey(name)) {
			final Query q = exclude.get(name);
			if (q.holdsFor(a)) {
				return -1;
			}
		}
		return 0;
	}

	public List<String> getRules() {
		final List<String> res = new LinkedList<String>();
		for (final Entry<String, Query> e : include.entrySet()) {
			e.getValue().addTo(res, "+ " + e.getKey());
		}
		for (final Entry<String, Query> e : exclude.entrySet()) {
			e.getValue().addTo(res, "- " + e.getKey());
		}
		return res;
	}

	@Override
	public String toString() {
		return originalName;
	}

}
