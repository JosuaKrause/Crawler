package de.visone.crawl.out;

import java.util.Iterator;
import java.util.Scanner;

/**
 * Reads input line by line and skips empty lines. The number of empty lines can
 * be obtained. A line can also be pushed back.
 * 
 * The iterator can only run once.
 * 
 * @author joschi
 * 
 */
public class ContentReader implements Iterable<String>, Iterator<String> {

	/** The input. */
	private Scanner in;

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
		if (in == null) {
			return;
		}
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
		if (!in.hasNextLine()) {
			in.close();
			in = null;
		}
	}

	/**
	 * @return Whether there are any more non-empty lines of input. Note that
	 *         after a call to this method {@link #emptyLines()} already returns
	 *         the new value.
	 */
	@Override
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
	@Override
	public String next() {
		if (pushedBack) {
			pushedBack = false;
			return last;
		}
		fetchNext();
		return last;
	}

	/**
	 * @return Returns the number of empty lines skipped by the last call of
	 *         {@link #next()}.
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
	public Iterator<String> iterator() {
		return this;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove is not allowed");
	}

	@Override
	protected void finalize() throws Throwable {
		if (in != null) {
			in.close();
			in = null;
		}
		super.finalize();
	}

}
