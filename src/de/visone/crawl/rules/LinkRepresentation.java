package de.visone.crawl.rules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.visone.crawl.gui.editor.Node;
import de.visone.crawl.sys.Link;

public class LinkRepresentation {

	private static class Span {
		int start;
		int end;

		Span(final int start, final int end) {
			this.start = start;
			this.end = end;
		}
	}

	public static int NORMAL = 0, INCLUDED = 1, EXCLUDED = 2, BLACKLISTED = 4;

	private final Map<Node, Span> ref = new HashMap<Node, Span>();

	private final Link[] links;

	private final int[] states;

	public LinkRepresentation(final Node root) {
		final List<Link> list = new LinkedList<Link>();
		createStrings(list, root);
		links = list.toArray(new Link[list.size()]);
		int i = links.length;
		states = new int[i];
		while (--i >= 0) {
			states[i] = NORMAL;
		}
	}

	private void createStrings(final List<Link> list, final Node n) {
		final int start = list.size();
		list.addAll(n.getLinks());
		ref.put(n, new Span(start, list.size()));
		for (final Node c : n.getChilds()) {
			createStrings(list, c);
		}
	}

	public void refreshStates(final Node n, HtmlQuery q,
			final BlacklistFilter bl) {
		final int a = n.accepts(q);
		if (a < 0) {
			q = null;
		}
		final Span s = ref.get(n);
		if (s != null) {
			for (int i = s.start; i < s.end; ++i) {
				final Link l = links[i];
				states[i] = NORMAL;
				if (bl.getRejectRule(l.getUrl()) != null) {
					states[i] |= BLACKLISTED;
				}
				if (a != 0) {
					states[i] |= a < 0 ? EXCLUDED : INCLUDED;
				}
			}
		}
		for (final Node c : n.getChilds()) {
			refreshStates(c, q, bl);
		}
	}

	public int getState(final int i) {
		return states[i];
	}

	public Link[] getLinks() {
		return links;
	}

	public Link getLink(final int i) {
		return links[i];
	}

	public int size() {
		return links.length;
	}

}
