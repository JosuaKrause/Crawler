package de.visone.crawl.out;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.visone.crawl.sys.Utils;

public abstract class XmlWriter implements CrawlListener {

	protected final XMLStreamWriter xml;

	public XmlWriter(final OutputStream out) throws IOException {
		try {
			xml = XMLOutputFactory.newFactory().createXMLStreamWriter(out,
					Utils.UTF8);
		} catch (final XMLStreamException e) {
			throw new IOException(e);
		} catch (final FactoryConfigurationError e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void pageCrawled(final Content c) {
		try {
			crawled(c);
		} catch (final XMLStreamException e) {
			e.printStackTrace();
		}
	}

	protected abstract void crawled(Content c) throws XMLStreamException;

	@Override
	public void close() throws IOException {
		try {
			xml.close();
		} catch (final XMLStreamException e) {
			throw new IOException(e);
		}
	}

}
