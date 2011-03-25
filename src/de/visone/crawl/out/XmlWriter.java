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

	public XmlWriter(final OutputStream out, final String root)
			throws IOException {
		try {
			xml = XMLOutputFactory.newInstance().createXMLStreamWriter(out,
					Utils.UTF8);
			xml.writeStartDocument();
			xml.writeStartElement(root);
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
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	protected abstract void crawled(Content c) throws XMLStreamException,
			IOException;

	@Override
	public void close() throws IOException {
		try {
			xml.writeEndElement();
			xml.writeEndDocument();
			xml.close();
		} catch (final XMLStreamException e) {
			throw new IOException(e);
		}
	}

}
