/**
 * 
 */
package de.visone.crawl.sys;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * A generic push back iterator.
 * 
 * Once an item has been read it can be pushed back onto the iterator so that
 * the next call to {@link #next()} returns the pushed item.
 * 
 * The iterator can only be used until the whole input has been read. Therefore
 * items cannot be removed, the iterator itself cannot be reseted or be used
 * twice.
 * 
 * Because of this behavior constant memory usage can be guaranteed.
 * 
 * @author Joschi
 * @param <T>
 *            The type of elements to iterate over.
 * 
 */
public abstract class PushbackIterator<T> implements Iterable<T>, Iterator<T>,
		Closeable {

	/** The last element. */
	private T last;

	/** Whether the input is pushed back. */
	private boolean pushedBack;

	/**
	 * Creates a new push-back iterator.
	 */
	public PushbackIterator() {
		last = null;
		pushedBack = false;
	}

	/**
	 * Fetches the next item of the input. The method is called every time a new
	 * input item is needed. During the call of this method the iterator is
	 * guaranteed to be in a not pushed back state.
	 * 
	 * @return The next item or <code>null</code> if the iterator is at its end.
	 *         In this case the method {@link #closeInput()} is called
	 *         afterwards.
	 */
	protected abstract T fetchNext();

	/**
	 * @return Whether there are any more elements in the input.
	 */
	@Override
	public boolean hasNext() {
		if (!pushedBack) {
			last = fetchNext();
			pushBack();
			if (last == null) {
				closeInput();
			}
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
		if (last == null) {
			closeInput();
		}
		return last;
	}

	/**
	 * Pushes the last element back on the input. Successive calls have no
	 * effect.
	 */
	public void pushBack() {
		pushedBack = true;
	}

	/**
	 * This method should close the input generator. It is called after
	 * {@link #fetchNext()} returns <code>null</code> to guaranty that it is
	 * closed as soon as possible. This method may be called multiple times.
	 */
	protected abstract void closeInput();

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
		closeInput();
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
