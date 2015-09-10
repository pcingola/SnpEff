package ca.mcgill.mcb.pcingola.fileIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ca.mcgill.mcb.pcingola.util.Gpr;

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
	protected boolean hasBackslashR;
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
	 * Get position within file
	 */
	public long getFilePointer() {
		if (!hasSeek()) throw new RuntimeException("Operation not supported"); // Operation not supported

		long pos = ((SeekableBufferedReader) reader).getFilePointer();
		if (nextLine == null) return pos;

		// Has a line been already loaded? We need to discount
		// those bytes (they haven't been delivered yet)
		return pos - nextLine.length() - (hasBackslashR ? 2 : 1);
	}

	public String getLine() {
		return line;
	}

	public int getLineNum() {
		return lineNum;
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
	 * @param inOffset
	 */
	protected void init(String fileName, int inOffset) {
		line = null;
		lineNum = 0;
		next = null;
		this.fileName = fileName;
		if (fileName != null) reader = Gpr.reader(fileName);
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	/**
	 * Load all elements from a file into a list
	 */
	public List<T> load() {
		LinkedList<T> list = new LinkedList<T>();
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
			return removeBackslashR(nl);
		}

		nextLine = reader.readLine(); // Read a line (only if needed)
		if (nextLine != null) lineNum++;
		return removeBackslashR(nextLine);
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
	 * Remove trailing '\r'
	 */
	protected String removeBackslashR(String line) {
		if ((line != null) //
				&& (line.length() > 0) //
				&& line.charAt(line.length() - 1) == '\r' //
		) {
			hasBackslashR = true;
			line = line.substring(0, line.length() - 1);
		}

		return line;
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
