package de.visone.crawl.out;

import java.io.PrintStream;

public class TextWriter extends SingleCSVWriter {

	public TextWriter(final PrintStream out) {
		super(out);
	}

	@Override
	public void pageCrawled(final Content c) {
		writeRaw(c.getText());
	}

}
