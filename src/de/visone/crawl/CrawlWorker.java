package de.visone.crawl;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import de.visone.crawl.out.Content;
import de.visone.crawl.out.XmlWriter;

public class CrawlWorker extends XmlWriter {

	public CrawlWorker(final OutputStream out) throws IOException {
		super(out);
	}

	/**
	 * @return The Settings configuration for this crawler.
	 */
	public Settings getSettings() {
		final Settings s = new Settings();
		// TODO: configure settings
		return s;
	}

	@Override
	protected void crawled(final Content c) throws XMLStreamException {
		// TODO Auto-generated method stub

	}
}
