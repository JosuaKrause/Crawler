package de.visone.crawl.out;

import java.io.IOException;
import java.util.Scanner;

import de.visone.crawl.sys.PushbackReader;

/**
 * Reads input line by line and skips empty lines. The number of empty lines can
 * be obtained. A line can also be pushed back.
 * 
 * The iterator can only run once.
 * 
 * @author Joschi
 * 
 */
public class ContentReader extends PushbackReader<String> {

	/** The input. */
	private Scanner in;

	/** The last number of empty lines. */
	private int lastSpace;

	/**
	 * Creates a content reader from a String.
	 * 
	 * @param content
	 *            The content String.
	 */
	public ContentReader(final String content) {
		this(new Scanner(content));
	}

	/**
	 * General purpose constructor.
	 * 
	 * @param scanner
	 *            The scanner to use.
	 */
	public ContentReader(final Scanner scanner) {
		in = scanner;
		lastSpace = 0;
	}

	@Override
	protected String fetchNext() {
		if (in == null) {
			return null;
		}
		int space = 0;
		String line = "";
		while (in.hasNextLine()) {
			line = in.nextLine().trim();
			if (!line.isEmpty()) {
				break;
			}
			++space;
		}
		lastSpace = space;
		if (!in.hasNextLine()) {
			smoothClose();
		}
		return line.isEmpty() ? null : line;
	}

	/**
	 * @return Returns the number of empty lines skipped by the last call of
	 *         {@link #next()}.
	 */
	public int emptyLines() {
		return lastSpace;
	}

	private void smoothClose() {
		if (in != null) {
			in.close();
			in = null;
		}
	}

	@Override
	public void close() throws IOException {
		smoothClose();
		super.close();
	}

}
