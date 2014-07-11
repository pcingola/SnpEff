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

	protected boolean debug = false;
	protected boolean autoClose = true;
	protected int lineNum;
	protected T next;
	protected BufferedReader reader;
	protected String fileName;
	protected String line;
	protected String nextLine;
	protected long fileSize = -1;

	public FileIterator(BufferedReader reader) {
		init(null, 0);
		this.reader = reader;
		autoClose = false;
	}

	public FileIterator(String fileName) {
		init(fileName, 0);
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
	 * @return
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
	 * @return
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
	 * @throws IOException
	 */
	protected String readLine() throws IOException {
		if (nextLine != null) {
			String nl = nextLine;
			nextLine = null;
			return nl;
		}

		nextLine = reader.readLine(); // Read a line (only if needed)

		// Remove trailing '\r'
		if ((nextLine != null) && (nextLine.length() > 0) && nextLine.charAt(nextLine.length() - 1) == '\r') nextLine = nextLine.substring(0, nextLine.length() - 1);

		if (nextLine != null) lineNum++;
		return nextLine;
	}

	/**
	 * Read next element
	 * @return
	 */
	protected abstract T readNext();

	/**
	 * Is reader ready? I.e. Can we read a line?
	 * @return
	 * @throws IOException
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
	 * @param pos
	 * @throws IOException
	 */
	public void seek(long pos) throws IOException {
		if (!hasSeek()) throw new IOException("Seek operation not supported!");
		((SeekableBufferedReader) reader).seek(pos);
		next = null;
	}

	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() //
				+ ":'" + fileName + "'" //
				+ ",autoClose:" + autoClose //
				+ (hasSeek() ? ",pos:" + ((SeekableBufferedReader) reader).position() : "") //
		;
	}
}
