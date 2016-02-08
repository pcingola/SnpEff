package org.snpeff.util;

import java.util.Iterator;

/**
 * Convert an iterator instance to a (fake) iterable
 * 
 * @author pablocingolani
 * @param <T>
 */
public class Iterator2Iterable<T> implements Iterable<T> {
	Iterator<T> iterator;

	public Iterator2Iterable(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public Iterator<T> iterator() {
		return iterator;
	}
}
