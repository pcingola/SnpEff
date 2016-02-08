package org.snpeff.fileIterator;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.snpeff.binseq.BinarySequence;

/**
 * Reads all sequences from a file
 * Warning: You should always call "close()" at the end of the iteration.
 * 
 * @author pcingola
 *
 * @param <T>
 */
public class BinSeqFileIterator<T extends BinarySequence> implements Iterable<T>, Iterator<T> {

	DataInputStream in;
	BinarySequence binRead;
	BinarySequence readerObject;

	public BinSeqFileIterator(String fileName) {
		try {
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
			binRead = null;
		} catch (FileNotFoundException e) {
			in = null;
		}
	}

	/**
	 * Close file (if not already done)
	 */
	public void close() {
		if (in != null) try {
			in.close();
			binRead = null;
		} catch (IOException e) {
		}
	}

	@Override
	public boolean hasNext() {
		if (in == null) return false; // No file? => We are done
		if (binRead != null) return true;

		try {
			binRead = readerObject.read(in);
			if (binRead == null) close();// Nothing more to read?
		} catch (IOException e) {
			binRead = null;
		}

		return (binRead != null);
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		BinarySequence bs = binRead;
		binRead = null;
		hasNext();
		return (T) bs;
	}

	@Override
	public void remove() {
		throw new RuntimeException("Unimplemented!");
	}

}
