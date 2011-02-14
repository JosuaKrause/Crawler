package de.visone.crawl.sys;

import java.net.URL;

import org.xml.sax.Attributes;

import de.visone.crawl.texter.Texter;

public class CrawlState implements Comparable<CrawlState> {

	private final int depth;

	private final URL base;

	private Texter texter;

	private Attributes curAttr;

	public CrawlState(final URL url, final int depth, final Texter texter) {
		if (url == null) {
			throw new NullPointerException("url");
		}
		this.depth = depth;
		this.texter = texter;
		base = url;
		curAttr = null;
	}

	public int getDepth() {
		return depth;
	}

	public URL getURL() {
		return base;
	}

	public Texter getTexter() {
		return texter;
	}

	public void setAttributes(final Attributes curAttr) {
		this.curAttr = curAttr;
	}

	public Attributes getAttributes() {
		return curAttr;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof CrawlState) {
			return ((CrawlState) obj).base.equals(base);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return base.hashCode();
	}

	@Override
	public int compareTo(final CrawlState o) {
		final int cmp = ((Integer) depth).compareTo(o.depth);
		return (cmp == 0) ? base.toString().compareTo(o.base.toString()) : cmp;
	}

	public void dispose() {
		curAttr = null;
		if (texter != null) {
			texter.dispose();
		}
		texter = null;
	}

}
