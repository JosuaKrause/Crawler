package de.visone.crawl.out;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;

import de.visone.crawl.Settings;
import de.visone.crawl.sys.Img;
import de.visone.crawl.sys.Link;
import de.visone.crawl.sys.Utils;

public class CrawlWorker extends XmlWriter {

	private static final String IMG_EXT = ".jpg";

	private static final String FORMAT = "jpg";

	private final File imgDump;

	private BlockContent cur;

	private Object curParent;

	public CrawlWorker(final OutputStream out, final File dump)
			throws IOException {
		super(out, "results");
		imgDump = new File(dump, "imgs/");
		Utils.ensureDir(imgDump);
		cur = null;
		curParent = null;
	}

	private Settings settings = null;

	/**
	 * @return The Settings configuration for this crawler.
	 */
	public Settings getSettings() {
		if (settings == null) {
			settings = new Settings();
			settings.domainSpecific = true;
			settings.onlySameHost = false;
			settings.doLinks = true;
			settings.doText = true;
			settings.killLimit = 0;
			settings.maxDepth = 20;
		}
		return settings;
	}

	@Override
	protected void crawled(final Content c) throws XMLStreamException,
			IOException {
		final Object p = c.getParent();
		if (curParent != null && p != null && p != curParent) {
			throw new RuntimeException("wrong ordering of URLs");
		}
		if (p == null) { // new Block
			if (cur != null) {
				cur.write();
			}
			xml.flush();
			curParent = c;
			cur = new BlockContent(c.getURL());
		}
		handleText(c.getText());
		handleLinks(c.getAcceptedLinks());
		handleImgs(c.getImages());
	}

	private void handleImgs(final Img[] imgs) throws IOException {
		final Settings set = getSettings();
		for (final Img i : imgs) {
			final File dest = getFileForImg(i);
			cur.addImg(i);
			if (dest.exists()) {
				continue;
			}
			final BufferedImage img = ImageIO.read(Utils.createInputStream(
					i.getSource(), set.userAgent));
			ImageIO.write(img, FORMAT, dest);
			img.flush();
		}
	}

	private void handleLinks(final Link[] links) {
		for (final Link l : links) {
			cur.addLink(l);
		}
	}

	private void handleText(final String text) {
		cur.addText(text);
	}

	private File getFileForImg(final Img i) {
		return new File(imgDump, i.getHashedName() + IMG_EXT);
	}

	@Override
	public void close() throws IOException {
		if (cur != null) {
			try {
				cur.write();
			} catch (final XMLStreamException e) {
				throw new IOException(e);
			}
			cur = null;
		}
		super.close();
	}

	private class BlockContent {

		final List<Link> links;

		final List<Img> imgs;

		final StringBuilder text;

		final URL base;

		BlockContent(final URL base) {
			this.base = base;
			links = new LinkedList<Link>();
			imgs = new LinkedList<Img>();
			text = new StringBuilder();
		}

		void addLink(final Link l) {
			links.add(l);
		}

		void addImg(final Img i) {
			imgs.add(i);
		}

		void addText(final String str) {
			if (text.length() != 0) {
				text.append(" ");
			}
			text.append(str);
		}

		void write() throws XMLStreamException, IOException {
			xml.writeStartElement("item");
			xml.writeStartElement("fullhtml");
			xml.writeAttribute("srcURL", base.toString());
			xml.writeStartElement("childs");
			for (final Link link : links) {
				xml.writeStartElement("child");
				xml.writeAttribute("url", link.getUrl().toString());
				for (final String s : link.getText()) {
					if (s.isEmpty()) {
						continue;
					}
					xml.writeStartElement("text");
					xml.writeCData(s);
					xml.writeEndElement();
				}
				xml.writeEndElement();
			}
			xml.writeEndElement();
			xml.writeStartElement("fulltext");
			xml.writeCData(text.toString().trim());
			xml.writeEndElement();
			xml.writeStartElement("images");
			for (final Img img : imgs) {
				xml.writeStartElement("img");
				xml.writeAttribute("srcURL", img.getSource().toString());
				xml.writeAttribute("localPath", getFileForImg(img)
						.getCanonicalFile().getAbsolutePath().toString());
				for (final String s : img.getAlts()) {
					if (s.isEmpty()) {
						continue;
					}
					xml.writeStartElement("alt");
					xml.writeCData(s);
					xml.writeEndElement();
				}
				for (final URL u : img.getParents()) {
					xml.writeEmptyElement("referer");
					xml.writeAttribute("url", u.toString());
				}
				xml.writeEndElement();
			}
			xml.writeEndElement();
			xml.writeEndElement();
			xml.writeEndElement();
		}
	}
}
