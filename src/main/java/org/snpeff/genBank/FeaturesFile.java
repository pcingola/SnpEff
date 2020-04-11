package org.snpeff.genBank;

import java.util.Iterator;

import org.snpeff.fileIterator.LineFileIterator;
import org.snpeff.util.Gpr;

/**
 * A file containing one or more set of features (e.g. multiple chromosomes concatenated in a single file)
 * Reference:
 * 		http://www.insdc.org/documents/feature-table
 *
 * @author pcingola
 *
 */
public abstract class FeaturesFile implements Iterable<Features>, Iterator<Features> {

	LineFileIterator lineFileIterator;
	Features next;

	public FeaturesFile(String fileName) {
		open(fileName);
		next = readNext();
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public Iterator<Features> iterator() {
		return this;
	}

	@Override
	public Features next() {
		Features n = next;
		next = readNext();
		if (!lineFileIterator.hasNext() && next.isEmpty()) next = null; // Are we done?
		return n;
	}

	/**
	 * Open a file
	 */
	protected void open(String fileName) {
		if (!Gpr.canRead(fileName)) throw new RuntimeException("Cannot read file '" + fileName + "'");
		if (lineFileIterator != null) lineFileIterator.close();
		lineFileIterator = new LineFileIterator(fileName);
	}

	abstract Features readNext();

	@Override
	public void remove() {
		throw new RuntimeException("Unimplemented method!");
	}

}
