package de.visone.crawl.out;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public abstract class SingleCSVWriter implements CrawlListener {

	public static String COMMA = ";";

	public static String QUOT = "\"";

	private final PrintStream out;

	private boolean beginOfLine;

	public SingleCSVWriter(final PrintStream out) {
		this.out = out;
		beginOfLine = true;
	}

	public SingleCSVWriter(final OutputStream out) throws IOException {
		this(new PrintStream(out, true, "UTF-8"));
	}

	protected void writeField(final Object obj) {
		writeField(obj.toString());
	}

	protected void writeField(final String str) {
		if (beginOfLine) {
			beginOfLine = false;
		} else {
			out.print(COMMA);
		}
		out.print(QUOT);
		out.print(str.replace(QUOT, QUOT + QUOT));
		out.print(QUOT);
	}

	protected void writeRaw(final String text) {
		out.print(text);
	}

	protected void nextRow() {
		out.println();
		beginOfLine = true;
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

}
