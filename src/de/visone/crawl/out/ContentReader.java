package de.visone.crawl.out;

import java.io.Closeable;
import java.util.Scanner;

/**
 * Reads input line by line and skips empty lines. The number of empty lines can
 * be obtained. A line can also be pushed back.
 * 
 * @author joschi
 * 
 */
public class ContentReader implements Closeable {

	/** The input. */
	private final Scanner in;

	/** The last line. */
	private String last;

	/** The last number of empty lines. */
	private int lastSpace;

	/** Whether the input is pushed back. */
	private boolean pushedBack;

	/**
	 * Creates a content reader from a String.
	 * 
	 * @param content
	 *            The content String.
	 */
	public ContentReader(final String content) {
		in = new Scanner(content);
		last = null;
		lastSpace = 0;
		pushedBack = false;
		fetchNext();
	}

	/**
	 * Fetches the next line under the assumption that {@link #pushedBack} is
	 * <code>false</code>.
	 */
	private void fetchNext() {
		// assert !pushedBack
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
		last = line.isEmpty() ? null : line;
	}

	/**
	 * @return Whether there are any more non-empty lines of input. Note that
	 *         after a call to this method {@link #emptyLines()} already returns
	 *         the new value.
	 */
	public boolean hasNext() {
		if (!pushedBack) {
			fetchNext();
			pushBack();
		}
		return last != null;
	}

	/**
	 * @return Returns the next non-empty line of input.
	 */
	public String getNext() {
		if (pushedBack) {
			pushedBack = false;
			return last;
		}
		fetchNext();
		return last;
	}

	/**
	 * @return Returns the number of empty lines skipped by the last call of
	 *         {@link #getNext()}.
	 */
	public int emptyLines() {
		return lastSpace;
	}

	/**
	 * Pushes the last line back on the input. The number of skipped lines
	 * remains the same.
	 */
	public void pushBack() {
		pushedBack = true;
	}

	@Override
	public void close() {
		in.close();
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

}
