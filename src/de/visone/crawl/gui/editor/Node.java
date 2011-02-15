package de.visone.crawl.gui.editor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import org.xml.sax.Attributes;

import de.visone.crawl.rules.HtmlQuery;
import de.visone.crawl.sys.Img;
import de.visone.crawl.sys.Link;

public class Node implements TreeNode {

	private final String tag;

	private final Attributes atts;

	private final Node parent;

	private final List<Node> childs;

	private final List<Link> links;

	private final List<Img> imgs;

	private final Map<Node, String> after;

	private String toStr;

	public Node(final String tag, final Attributes atts, final Node parent) {
		if (atts == null) {
			throw new NullPointerException("atts");
		}
		this.tag = tag;
		this.atts = atts;
		this.parent = parent;
		if (parent != null) {
			parent.childs.add(this);
		}
		childs = new LinkedList<Node>();
		links = new LinkedList<Link>();
		imgs = new LinkedList<Img>();
		after = new HashMap<Node, String>();
		toStr = null;
	}

	public List<Node> getChilds() {
		return childs;
	}

	public String getText(final Node n) {
		final String s = after.get(n);
		return s == null ? "" : s;
	}

	public int accepts(final HtmlQuery q) {
		if (q == null) {
			return 0;
		}
		final int check = q.check(tag, atts);
		if (check < 0) {
			return -1;
		}
		if (parent == null) {
			return check;
		}
		final int pc = parent.accepts(q);
		if (pc < 0) {
			return 0;
		}
		return (pc | check) > 0 ? 1 : 0;
	}

	public void addText(String text) {
		final Node a = childs.isEmpty() ? null : childs.get(childs.size() - 1);
		if (after.containsKey(a)) {
			text = after.get(a) + text;
		}
		after.put(a, text.trim());
	}

	public void addLink(final Link lnk) {
		links.add(lnk);
	}

	public void addImg(final Img img) {
		imgs.add(img);
	}

	public void getDescendantLinks(final Set<Link> l) {
		getDescendantLinks(null, l, null);
	}

	public void getDescendantLinks(final Set<Link> accept,
			final Set<Link> other, HtmlQuery q) {
		final int a = accepts(q);
		if (a > 0) {
			accept.addAll(links);
		} else {
			if (a < 0) {
				q = null;
			}
			other.addAll(links);
		}
		for (final Node n : childs) {
			n.getDescendantLinks(accept, other, q);
		}
	}

	public void getDescendantImages(final Set<Img> i) {
		getDescendantImages(i, null);
	}

	public void getDescendantImages(final Set<Img> i, HtmlQuery q) {
		final int a = accepts(q);
		if (a > 0) {
			i.addAll(imgs);
		} else {
			if (a < 0) {
				q = null;
			}
		}
		for (final Node n : childs) {
			n.getDescendantImages(i, q);
		}
	}

	public List<Img> getImages() {
		return imgs;
	}

	public void getDescendantText(final StringBuilder sb) {
		if (after.containsKey(null)) {
			sb.append(after.get(null));
		}
		for (final Node n : childs) {
			n.getDescendantText(sb);
			if (after.containsKey(n)) {
				sb.append(after.get(n));
			}
		}
	}

	public List<Link> getLinks() {
		return links;
	}

	@Override
	public String toString() {
		if (toStr == null) {
			final StringBuilder sb = new StringBuilder(tag);
			final int len = atts.getLength();
			for (int i = 0; i < len; ++i) {
				sb.append(" ");
				sb.append(atts.getQName(i));
				sb.append("=\"");
				sb.append(atts.getValue(i));
				sb.append('"');
			}
			toStr = sb.toString();
		}
		return toStr;
	}

	@Override
	public Enumeration<TreeNode> children() {
		return new Vector<TreeNode>(childs).elements();
	}

	@Override
	public boolean getAllowsChildren() {
		return false;
	}

	@Override
	public TreeNode getChildAt(final int childIndex) {
		return childs.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return childs.size();
	}

	@Override
	public int getIndex(final TreeNode node) {
		return childs.indexOf(node);
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public boolean isLeaf() {
		return childs.isEmpty();
	}
}
