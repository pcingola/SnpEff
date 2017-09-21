package org.snpeff.fileIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.snpeff.util.Gpr;

/**
 * Opens a file and iterates over all objects <T> in the file
 * Note: The file is not loaded in memory, thus allows to iterate over very large files
 *
 * @author pcingola
 */
public abstract class FileIterator<T> implements Iterable<T>, Iterator<T> {

	protected boolean debug;
	protected boolean verbose;
	protected boolean autoClose;
	protected int countNewLineChars; // How many newline characters we use (e.g. '\n' = 1,  '\r\n' = 2)
	protected int lineNum;
	protected T next;
	protected BufferedReader reader;
	protected String fileName;
	protected String line; // Current line being processed
	protected String nextLine; // Next line to show
	protected long fileSize = -1;

	public FileIterator(BufferedReader reader) {
		init(null, 0);
		this.reader = reader;
		autoClose = false;
		guessNewLineChars();
	}

	public FileIterator(String fileName) {
		init(fileName, 0);
		autoClose = true;
	}

	/**
	 * Close file
	 */
	public void close() {
		try {
			if (reader != null) reader.close();
		} catch (IOException e) {
		}

		reader = null;
		line = null;
		lineNum = 0;
		next = null;
	}

	/**
	 * Guess number of newline characters used (e.g. '\n' or '\r\n'
	 */
	protected int countNewLineChars() {
		try {
			int c, cprev = 0;
			while ((c = reader.read()) != -1) {
				if (cprev == '\r') {
					if (c == '\n') return 2;
					return 1;
				} else if (c == '\n') return 1;

				cprev = c;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Reached end of stream and could not guess.
		return 0;
	}

	/**
	 * Get position within file
	 */
	public long getFilePointer() {
		if (!hasSeek()) throw new RuntimeException("Operation not supported"); // Operation not supported

		long pos = ((SeekableBufferedReader) reader).getFilePointer();
		if (nextLine == null) return pos;

		// Has a line been already loaded? We need to discount
		// those bytes (they haven't been delivered yet)
		return pos - nextLine.length() - countNewLineChars;
	}

	public String getLine() {
		return line;
	}

	public int getLineNum() {
		return lineNum;
	}

	/**
	 * Guess number of newline characters used (e.g. '\n' or '\r\n'
	 * Make sure we return to the current read position
	 */
	protected void guessNewLineChars() {
		if (!hasSeek()) return; // Can only guess if have 'seek' operation

		try {
			long pos = ((SeekableBufferedReader) reader).getFilePointer();
			countNewLineChars = countNewLineChars();
			seek(pos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasNext() {
		if (reader == null) return false; // No input stream?

		if (next == null) {
			next = readNext(); // Try reading next item.
			if ((next == null) && autoClose) close(); // End of file or any problem? => Close file
		}

		return (next != null);
	}

	/**
	 * Is 'seek' operation supported?
	 */
	public boolean hasSeek() {
		return (reader instanceof SeekableBufferedReader);
	}

	/**
	 * Initialize
	 * @param fileName : Can be null (no file is opened)
	 */
	protected void init(String fileName, int inOffset) {
		line = null;
		lineNum = 0;
		next = null;
		this.fileName = fileName;
		if (fileName != null) reader = Gpr.reader(fileName);
	}

	public boolean isDebug() {
		return debug;
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	/**
	 * Load all elements from a file into a list
	 */
	public List<T> load() {
		LinkedList<T> list = new LinkedList<>();
		for (T t : this)
			list.add(t);
		close();
		return list;
	}

	@Override
	public T next() {
		if (hasNext()) {
			T ret = next;
			next = null;
			return ret;
		}
		return null;
	}

	/**
	 * Read a line from reader
	 */
	protected String readLine() throws IOException {
		if (nextLine != null) {
			String nl = nextLine;
			nextLine = null;
			return nl;
		}

		nextLine = reader.readLine(); // Read a line (only if needed)
		if (nextLine != null) lineNum++;
		return nextLine;
	}

	/**
	 * Read next element
	 */
	protected abstract T readNext();

	/**
	 * Is reader ready? I.e. Can we read a line?
	 */
	protected boolean ready() throws IOException {
		if (reader == null) return false; // No reader? then we are not ready
		if (nextLine != null) return true; // Next line is null? then we have to try to read a line (to see if one is available)
		readLine();
		return nextLine != null; // Line was read from the file? Then we are ready.
	}

	@Override
	public void remove() {
		throw new RuntimeException("Unimplemented");
	}

	/**
	 * Seek to 'pos' (jump to byte number 'pos' in the file
	 */
	public void seek(long pos) throws IOException {
		if (!hasSeek()) throw new IOException("Seek operation not supported!");
		((SeekableBufferedReader) reader).seek(pos);
		next = null;
		nextLine = null;
	}

	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() //
				+ ":'" + fileName + "'" //
				+ ",autoClose:" + autoClose //
				+ (hasSeek() ? ",pos:" + ((SeekableBufferedReader) reader).getFilePointer() : "") //
		;
	}
}
