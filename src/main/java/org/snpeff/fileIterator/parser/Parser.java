package org.snpeff.fileIterator.parser;

import java.util.Collection;

/**
 * Parse a string and return a collection of objects.
 * Note: In most cases the collection will only have one element.
 * 
 * @author pcingola
 */
public interface Parser<T> {

	public Collection<T> parse(String str);
}
