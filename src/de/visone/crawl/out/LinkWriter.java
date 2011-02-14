package de.visone.crawl.out;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import de.visone.crawl.sys.Link;
import de.visone.crawl.sys.Utils;

public class LinkWriter extends SingleCSVWriter {

	private static final File BASE = new File("contents/");

	public LinkWriter(final PrintStream out) {
		super(out);
	}

	@Override
	public void pageCrawled(final Content c) {
		final URL source = c.getURL();
		for (final Link l : c.getAcceptedLinks()) {
			writeLink(l, source);
		}
		for (final Link l : c.getOtherLinks()) {
			writeLink(l, source);
		}
		final String txt = c.getText().trim();
		if (!txt.isEmpty()) {
			final File dest = Utils.toFile(source, BASE, "txt");
			Utils.ensureDir(dest.getParentFile());
			try {
				final PrintStream ps = new PrintStream(dest, "UTF-8");
				ps.println(txt);
				ps.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeLink(final Link link, final URL source) {
		final URL target = link.getUrl();
		for (final String label : link.getText()) {
			writeField(source);
			writeField(target);
			writeField(label);
			nextRow();
		}
	}

}
