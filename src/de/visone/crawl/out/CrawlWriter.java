package de.visone.crawl.out;

import static de.visone.crawl.sys.Utils.NL;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.visone.crawl.sys.Link;
import de.visone.crawl.sys.Utils;

public class CrawlWriter implements CrawlListener {

	private final File base;

	public CrawlWriter(final File base) {
		this.base = base;
	}

	@Override
	public void pageCrawled(final Content c) {
		final File f = Utils.toFile(c.getURL(), base, "txt");
		Utils.ensureDir(f.getParentFile());
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
			writeContent(fw, c);
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void writeContent(final FileWriter fw, final Content c)
			throws IOException {
		fw.append(c.getURL().toString());
		fw.append(NL);
		for (final Link l : c.getAcceptedLinks()) {
			writeLink(fw, l);
		}
		for (final Link l : c.getOtherLinks()) {
			writeLink(fw, l);
		}
		final String txt = c.getText();
		if (!txt.isEmpty()) {
			fw.append(NL);
			fw.append(txt);
		}
	}

	private void writeLink(final FileWriter fw, final Link l)
			throws IOException {
		fw.append(l.getUrl().toString());
		for (final String name : l.getText()) {
			fw.append(' ');
			fw.append(name);
		}
		fw.append(NL);
	}

	@Override
	public void close() throws IOException {
		// nothing to do...
	}
}
