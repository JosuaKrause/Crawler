package de.visone.crawl.rules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.visone.crawl.gui.editor.Node;
import de.visone.crawl.sys.Img;

public class ImageRepresentation {

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

	private final Img[] imgs;

	private final int[] states;

	public ImageRepresentation(final Node root) {
		final List<Img> list = new LinkedList<Img>();
		createStrings(list, root);
		imgs = list.toArray(new Img[list.size()]);
		int i = imgs.length;
		states = new int[i];
		while (--i >= 0) {
			states[i] = NORMAL;
		}
	}

	private void createStrings(final List<Img> list, final Node n) {
		final int start = list.size();
		list.addAll(n.getImages());
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
				final Img l = imgs[i];
				states[i] = NORMAL;
				if (bl.getRejectRule(l.getSource()) != null) {
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

	public Img[] getImages() {
		return imgs;
	}

	public Img getImage(final int i) {
		return imgs[i];
	}

	public int size() {
		return imgs.length;
	}

}
