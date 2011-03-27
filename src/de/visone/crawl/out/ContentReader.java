package de.visone.crawl.out;

import java.util.Scanner;

import de.visone.crawl.sys.PushbackIterator;

/**
 * Reads input line by line and skips empty lines. The number of empty lines can
 * be obtained. A line can also be pushed back.
 * 
 * The iterator can only run once. More information on how this iterator works
 * see {@link PushbackIterator}.
 * 
 * @author Joschi
 * 
 */
public class ContentReader extends PushbackIterator<String> {

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
		return line.isEmpty() ? null : line;
	}

	/**
	 * @return Returns the number of empty lines skipped by the last call of
	 *         {@link #next()}. Note that after a call of {@link #hasNext()}
	 *         this method already returns the next value and that the result
	 *         {@link #pushBack()} has no effect on this result either.
	 */
	public int emptyLines() {
		return lastSpace;
	}

	@Override
	protected void closeInput() {
		if (in != null) {
			in.close();
			in = null;
		}
	}

}
