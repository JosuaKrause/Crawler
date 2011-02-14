package de.visone.crawl.google;

import java.io.IOException;
import java.io.OutputStream;

import de.visone.crawl.out.Content;
import de.visone.crawl.out.SingleCSVWriter;

public class GoogleWriter extends SingleCSVWriter {

	public GoogleWriter(final OutputStream out) throws IOException {
		super(out);
	}

	@Override
	public void pageCrawled(final Content c) {
		String txt = c.getText();
		txt = txt.replace(".", "");
		txt = txt.replace(",", "");
		txt = txt.replaceAll("[^0-9]", "");
		final String u = c.getURL().toString();
		final int start = u.indexOf("q=");
		int end = u.indexOf('&', start);
		if (end < 0) {
			end = u.length();
		}
		try {
			final double d = Double.parseDouble(txt);
			writeField(u.substring(start + 2, end));
			writeField(d);
			nextRow();
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Text was: " + txt, e);
		}
	}

}
