package de.visone.crawl.sys;

import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * An Object representing a simple Image given by an URL. The identity is given
 * by the URL only.
 * 
 * @author Joschi
 * 
 */
public class Img {

	private final URL src;

	private final Set<URL> parent;

	private final List<String> alt;

	public Img(final URL src) {
		if (src == null) {
			throw new NullPointerException("src");
		}
		this.src = src;
		parent = new HashSet<URL>();
		alt = new LinkedList<String>();
	}

	public URL getSource() {
		return src;
	}

	public URL[] getParents() {
		return parent.toArray(new URL[parent.size()]);
	}

	public String[] getAlts() {
		return alt.toArray(new String[alt.size()]);
	}

	public void add(final URL p, final String a) {
		if (p == null) {
			throw new NullPointerException("p");
		}
		parent.add(p);
		if (a == null) {
			return;
		}
		alt.add(a);
	}

	public String getHashedName() {
		return Utils.getCRCHash(src.toString());
	}

	@Override
	public int hashCode() {
		return src.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Img)) {
			return false;
		}
		return src.equals(((Img) obj).src);
	}

	@Override
	public String toString() {
		return src.toString();
	}

}
