package de.visone.crawl.texter;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;

import de.visone.crawl.gui.editor.Node;
import de.visone.crawl.sys.CrawlState;
import de.visone.crawl.sys.Link;
import de.visone.crawl.sys.UrlPool;

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
	public void link(final URL link, final String text, final UrlPool pool,
			final CrawlState state) {
		pool.append(link, state);
		stack.peek().addLink(new Link(link));
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
	public void dispose() {
		root = null;
	}

}
