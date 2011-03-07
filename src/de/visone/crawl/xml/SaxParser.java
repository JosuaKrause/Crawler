package de.visone.crawl.xml;

import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.visone.crawl.sys.AbstractUrlPool;
import de.visone.crawl.sys.CrawlState;
import de.visone.crawl.sys.Utils;
import de.visone.crawl.texter.Texter;

public class SaxParser extends DefaultHandler {

	private final CrawlState state;

	private final AbstractUrlPool pool;

	private URL link;

	private StringBuilder linkText;

	public SaxParser(final CrawlState state, final AbstractUrlPool pool) {
		this.state = state;
		this.pool = pool;
		link = null;
		linkText = null;
	}

	@Override
	public void startElement(final String uri, final String localName,
			final String qName, final Attributes attributes)
			throws SAXException {
		final Texter t = state.getTexter();
		t.startTag(qName, attributes);
		if (qName.equals("a")) {
			final String href = attributes.getValue("href");
			if (href != null) {
				link = Utils.getURL(href.trim(), state.getURL());
				linkText = new StringBuilder();
			}
		} else if (qName.equals("img")) {
			final String src = attributes.getValue("src");
			if (src != null) {
				final URL link = Utils.getURL(src.trim(), state.getURL());
				t.img(link, attributes.getValue("alt"), pool, state);
			}
		}
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String qName) throws SAXException {
		final Texter t = state.getTexter();
		if (qName.equals("a") && link != null) {
			t.link(link, linkText.toString().replaceAll("\\s+", " ").trim(),
					pool, state);
			link = null;
			linkText = null;
		}
		t.endTag(qName);
	}

	@Override
	public void characters(final char[] ch, final int start, final int length)
			throws SAXException {
		final Texter t = state.getTexter();
		if (t.acceptString()) {
			t.string(new String(ch, start, length));
		}
		if (link != null) {
			linkText.append(ch, start, length);
		}
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start,
			final int length) throws SAXException {
		final Texter t = state.getTexter();
		if (t.acceptString()) {
			t.string(" ");
		}
		if (link != null) {
			linkText.append(" ");
		}
	}

}
