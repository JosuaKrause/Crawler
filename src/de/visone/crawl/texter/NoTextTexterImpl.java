package de.visone.crawl.texter;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;

import de.visone.crawl.rules.HtmlQuery;
import de.visone.crawl.sys.AbstractUrlPool;
import de.visone.crawl.sys.CrawlState;
import de.visone.crawl.sys.Img;
import de.visone.crawl.sys.Link;

public class NoTextTexterImpl implements Texter {

	private final URL url;

	private final Map<URL, Link> accepted;

	private final Map<URL, Link> other;

	private final Map<URL, Img> images;

	private int acceptLinks;

	private int exceptLinks;

	private final HtmlQuery links;

	private final Stack<Integer> linkStack;

	private Object parent;

	public NoTextTexterImpl(final URL url, final HtmlQuery links) {
		this.url = url;
		this.links = links;
		accepted = links == null ? null : new HashMap<URL, Link>();
		other = links == null ? null : new HashMap<URL, Link>();
		images = links == null ? null : new HashMap<URL, Img>();
		acceptLinks = exceptLinks = 0;
		linkStack = links == null ? null : new Stack<Integer>();
		parent = null;
	}

	@Override
	public boolean acceptString() {
		return false;
	}

	@Override
	public void startTag(final String tag, final Attributes a) {
		if (links == null) {
			return;
		}
		final int l = links.check(tag, a);
		if (l > 0) {
			++acceptLinks;
		} else if (l < 0) {
			++exceptLinks;
		}
		linkStack.push(l);
	}

	@Override
	public void endTag(final String tag) {
		if (links == null) {
			return;
		}
		final int l = linkStack.pop();
		if (l > 0) {
			--acceptLinks;
		} else if (l < 0) {
			--exceptLinks;
		}
	}

	@Override
	public void string(final String str) {
		// ignore strings
	}

	@Override
	public String getText() {
		return "";
	}

	@Override
	public Link[] getAcceptedLinks() {
		if (links == null) {
			return new Link[0];
		}
		return accepted.values().toArray(new Link[accepted.size()]);
	}

	@Override
	public Link[] getOtherLinks() {
		if (links == null) {
			return new Link[0];
		}
		return other.values().toArray(new Link[other.size()]);
	}

	@Override
	public URL getURL() {
		return url;
	}

	protected boolean acceptLink() {
		return acceptLinks > 0 && exceptLinks <= 0;
	}

	@Override
	public void link(final URL link, final String name,
			final AbstractUrlPool pool, final CrawlState state) {
		if (links == null) {
			return;
		}
		if (link == null) {
			return;
		}
		if (!acceptLink()) {
			return;
		}
		if (pool.append(link, state)) {
			if (!accepted.containsKey(link)) {
				accepted.put(link, new Link(link));
			}
			accepted.get(link).add(name);
		} else {
			if (!other.containsKey(link)) {
				other.put(link, new Link(link));
			}
			other.get(link).add(name);
		}
	}

	@Override
	public void img(final URL img, final String text,
			final AbstractUrlPool pool, final CrawlState state) {
		if (!acceptLink()) {
			return;
		}
		if (!pool.append(img, state, true)) {
			return;
		}
		if (!images.containsKey(img)) {
			images.put(img, new Img(img));
		}
		images.get(img).add(state.getURL(), text);
	}

	@Override
	public Img[] getImages() {
		return images.values().toArray(new Img[images.size()]);
	}

	@Override
	public Object getParent() {
		return parent;
	}

	@Override
	public void setParent(final Texter c) {
		parent = c;
	}

	@Override
	public void dispose() {
		if (accepted != null) {
			accepted.clear();
		}
		if (other != null) {
			other.clear();
		}
		if (images != null) {
			images.clear();
		}
		parent = null;
	}
}
