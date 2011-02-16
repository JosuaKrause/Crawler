package de.visone.crawl.texter;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;

import de.visone.crawl.gui.editor.Node;
import de.visone.crawl.sys.AbstractUrlPool;
import de.visone.crawl.sys.CrawlState;
import de.visone.crawl.sys.Img;
import de.visone.crawl.sys.Link;

public class TreeTexterImpl implements Texter {

	private final URL url;

	private Node root;

	private final Stack<Node> stack;

	public TreeTexterImpl(final URL url) {
		this.url = url;
		stack = new Stack<Node>();
	}

	public Node getRoot() {
		return root;
	}

	@Override
	public boolean acceptString() {
		return true;
	}

	@Override
	public void startTag(final String tag, final Attributes a) {
		final Node n = new Node(tag, a, stack.isEmpty() ? null : stack.peek());
		stack.push(n);
		if (root == null) {
			root = n;
		}
	}

	@Override
	public void endTag(final String tag) {
		stack.pop();
	}

	@Override
	public void link(final URL link, final String text,
			final AbstractUrlPool pool, final CrawlState state) {
		pool.append(link, state);
		stack.peek().addLink(new Link(link));
	}

	@Override
	public void img(final URL img, final String text,
			final AbstractUrlPool pool, final CrawlState state) {
		stack.peek().addImg(new Img(img));
	}

	@Override
	public void string(final String str) {
		stack.peek().addText(str.replaceAll("\\s+", " ").trim());
	}

	@Override
	public Link[] getAcceptedLinks() {
		return getOtherLinks();
	}

	@Override
	public Link[] getOtherLinks() {
		final Set<Link> links = new HashSet<Link>();
		root.getDescendantLinks(links);
		return links.toArray(new Link[links.size()]);
	}

	@Override
	public Img[] getImages() {
		final Set<Img> imgs = new HashSet<Img>();
		root.getDescendantImages(imgs);
		return imgs.toArray(new Img[imgs.size()]);
	}

	@Override
	public String getText() {
		final StringBuilder sb = new StringBuilder();
		root.getDescendantText(sb);
		return sb.toString();
	}

	@Override
	public URL getURL() {
		return url;
	}

	@Override
	public Object getParent() {
		return null;
	}

	@Override
	public void setParent(final Texter c) {
		// not supported
	}

	@Override
	public void dispose() {
		root = null;
	}

}
