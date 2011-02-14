package de.visone.crawl.sys;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.visone.crawl.out.Content;

/**
 * This class represents one or more links from one page to another. The source
 * URL is not explicitly given. You can obtain it for example with
 * {@link Content#getURL()}.
 * 
 * @author Joschi
 * 
 */
public class Link {

	/**
	 * The target URL.
	 */
	private final URL url;

	/**
	 * The labels of the links.
	 */
	private final List<String> text;

	/**
	 * Creates a Link to a URL. Initially there are no labels.
	 * 
	 * @param url
	 *            The target URL.
	 */
	public Link(final URL url) {
		if (url == null) {
			throw new NullPointerException("url");
		}
		this.url = url;
		text = new ArrayList<String>();
	}

	/**
	 * @return The target URL.
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @return A list of all Labels.
	 */
	public List<String> getText() {
		return text;
	}

	/**
	 * Adds a label.
	 * 
	 * @param t
	 *            The label.
	 */
	public void add(final String t) {
		text.add(t);
	}

	/**
	 * Adds a list of labels.
	 * 
	 * @param t
	 *            The labels.
	 */
	public void add(final List<String> t) {
		text.addAll(t);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof Link) {
			return ((Link) obj).url.equals(url);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return url.hashCode();
	}

	@Override
	public String toString() {
		return url + " " + text.toString();
	}

}
