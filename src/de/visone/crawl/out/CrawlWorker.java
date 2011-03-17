package de.visone.crawl.out;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;

import de.visone.crawl.Settings;
import de.visone.crawl.sys.Img;
import de.visone.crawl.sys.Link;
import de.visone.crawl.sys.Utils;

public class CrawlWorker extends XmlWriter {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd");

	private static final String IMG_EXT = ".jpg";

	private static final String FORMAT = "jpg";

	private final File imgDump;

	private final File baseDump;

	private Map<URL, String> idMap;

	private BlockContent cur;

	private Object curParent;

	public CrawlWorker(final OutputStream out, final File dump)
			throws IOException {
		super(out, "results");
		baseDump = dump;
		imgDump = new File(dump, "imgs/" + DATE_FORMAT.format(new Date()) + "/");
		Utils.ensureDir(imgDump);
		cur = null;
		curParent = null;
		idMap = null;
	}

	public void setIdMap(final Map<URL, String> idMap) {
		this.idMap = idMap;
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
			settings.coolDown = 5000;
			settings.forcedTimeoutAfter = 10 * 60000;
			settings.maxRetries = 0;
			settings.haltOnError = false;
			settings.readNoFollow = true;
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
			final URL u = c.getURL();
			cur = new BlockContent(u, getId(u));
		}
		handleText(c.getText());
		handleLinks(c.getAcceptedLinks());
		handleImgs(c.getImages());
	}

	private String getId(final URL u) {
		if (idMap == null) {
			return null;
		}
		return idMap.get(u);
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

	private String getPathForImg(final Img i) {
		final File img = getFileForImg(i);
		return getRelativePath(baseDump, img);
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

		final String guid;

		final List<Link> links;

		final List<Img> imgs;

		final StringBuilder text;

		final URL base;

		BlockContent(final URL base, final String guid) {
			this.base = base;
			this.guid = guid;
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
			if (guid != null) {
				xml.writeAttribute("guid", guid);
			}
			xml.writeStartElement("fullhtml");
			xml.writeStartElement("fulltext");
			xml.writeAttribute("srcURL", base.toString());
			xml.writeCData(text.toString().trim());
			xml.writeEndElement();
			xml.writeStartElement("images");
			for (final Img img : imgs) {
				xml.writeStartElement("img");
				xml.writeAttribute("srcURL", img.getSource().toString());
				xml.writeAttribute("localPath", getPathForImg(img));
				xml.writeStartElement("alts");
				for (final String s : img.getAlts()) {
					if (s.isEmpty()) {
						continue;
					}
					xml.writeStartElement("alt");
					xml.writeCData(s);
					xml.writeEndElement();
				}
				xml.writeEndElement();
				xml.writeStartElement("referers");
				for (final URL u : img.getParents()) {
					xml.writeEmptyElement("referer");
					xml.writeAttribute("url", u.toString());
				}
				xml.writeEndElement();
				xml.writeEndElement();
			}
			xml.writeEndElement();
			xml.writeStartElement("childs");
			for (final Link link : links) {
				xml.writeStartElement("child");
				xml.writeAttribute("url", link.getUrl().toString());
				xml.writeStartElement("linktexts");
				for (final String s : link.getText()) {
					if (s.isEmpty()) {
						continue;
					}
					xml.writeStartElement("linktext");
					xml.writeCData(s);
					xml.writeEndElement();
				}
				xml.writeEndElement();
				xml.writeEndElement();
			}
			xml.writeEndElement();
			xml.writeEndElement();
			xml.writeEndElement();
		}
	}

	/**
	 * Breaks a path down into individual elements and adds it to a list.
	 * Example: if a path is /a/b/c/d.txt, the breakdown will be [d.txt,c,b,a]
	 * 
	 * @param f
	 *            input file
	 * @return a List collection with the individual elements of the path in
	 *         reverse order
	 * @author David M. Howard
	 */
	private static List<String> getPathList(final File f) {
		List<String> l = new ArrayList<String>();
		File r;
		try {
			r = f.getCanonicalFile();
			while (r != null) {
				l.add(r.getName());
				r = r.getParentFile();
			}
		} catch (final IOException e) {
			e.printStackTrace();
			l = null;
		}
		return l;
	}

	/**
	 * figure out a string representing the relative path of 'f' with respect to
	 * 'r'
	 * 
	 * @param r
	 *            home path
	 * @param f
	 *            path of file
	 * @author David M. Howard
	 */
	private static String matchPathLists(final List<String> r,
			final List<String> f) {
		int i;
		int j;
		String s;
		// start at the beginning of the lists
		// iterate while both lists are equal
		s = "";
		i = r.size() - 1;
		j = f.size() - 1;

		// first eliminate common root
		while ((i >= 0) && (j >= 0) && (r.get(i).equals(f.get(j)))) {
			i--;
			j--;
		}

		// for each remaining level in the home path, add a ..
		for (; i >= 0; i--) {
			s += ".." + File.separator;
		}

		// for each level in the file path, add the path
		for (; j >= 1; j--) {
			s += f.get(j) + File.separator;
		}

		// file name
		s += f.get(j);
		return s;
	}

	/**
	 * get relative path of File 'f' with respect to 'home' directory. example:
	 * home = /a/b/c f = /a/d/e/x.txt s = getRelativePath(home,f) =
	 * ../../d/e/x.txt
	 * 
	 * @param home
	 *            base path, should be a directory, not a file, or it doesn't
	 *            make sense
	 * @param f
	 *            file to generate path for
	 * @return path from home to f as a string
	 * @author David M. Howard
	 */
	public static String getRelativePath(final File home, final File f) {
		List<String> homelist;
		List<String> filelist;
		String s;

		homelist = getPathList(home);
		filelist = getPathList(f);
		s = matchPathLists(homelist, filelist);

		return s;
	}

}
