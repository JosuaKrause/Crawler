/**
 * 
 */
package de.visone.crawl.sys;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * A generic push back reader.
 * 
 * @author Joschi
 * @param <T>
 *            The type of elements to iterate over.
 * 
 */
public abstract class PushbackReader<T> implements Iterable<T>, Iterator<T>,
		Closeable {

	/** The last element. */
	private T last;

	/** Whether the input is pushed back. */
	private boolean pushedBack;

	public PushbackReader() {
		last = null;
		pushedBack = false;
	}

	protected abstract T fetchNext();

	/**
	 * @return Whether there are any more elements in the input.
	 */
	@Override
	public boolean hasNext() {
		if (!pushedBack) {
			last = fetchNext();
			pushBack();
		}
		return last != null;
	}

	/**
	 * @return Returns the next element from the input. Or the last element if
	 *         it was pushed back.
	 */
	@Override
	public T next() {
		if (pushedBack) {
			pushedBack = false;
			return last;
		}
		last = fetchNext();
		return last;
	}

	/**
	 * Pushes the last element back on the input.
	 */
	public void pushBack() {
		pushedBack = true;
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove is not allowed");
	}

	@Override
	public void close() throws IOException {
		if (last != null) {
			last = null;
			pushedBack = true;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

}
