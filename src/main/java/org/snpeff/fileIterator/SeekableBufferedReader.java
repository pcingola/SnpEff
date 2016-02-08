package org.snpeff.fileIterator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;

/**
 * A buffered reader for a file.
 * It allows to 'seek' and 'readLine()'
 *
 * @author pablocingolani
 */
public class SeekableBufferedReader extends BufferedReader {

	public static int DEFAULT_BUFFER_SIZE = 64 * 1024;

	byte buffer[];
	int next, last;
	int bufferSize;
	String fileName;
	RandomAccessFile randomAccFile;
	long latestpos;

	public SeekableBufferedReader(String fileName) throws IOException {
		super(new NullReader());
		init(fileName, DEFAULT_BUFFER_SIZE);
	}

	public SeekableBufferedReader(String fileName, int bufferSize) throws IOException {
		super(null);
		init(fileName, bufferSize);
	}

	@Override
	public void close() throws IOException {
		if (randomAccFile != null) {
			randomAccFile.close();
			randomAccFile = null;
			latestpos = -1;
			next = last = 0;
		}
	}

	@Override
	public boolean equals(Object arg0) {
		throw new RuntimeException("Unimplemented method!");
	}

	/**
	 * Find a '\n' in the buffer.
	 *
	 * @return Position of '\n' in the buffer or -1 if not found
	 */
	int findNl(int next, int last) {
		for (int i = next; i < last; i++) {
			if (buffer[i] == '\n') return i;
		}
		return -1;
	}

	public long getFilePointer() {
		return latestpos + next;
	}

	@Override
	public int hashCode() {
		throw new RuntimeException("Unimplemented method!");
	}

	/**
	 * Initialize
	 */
	void init(String fileName, int bufferSize) throws IOException {
		this.bufferSize = bufferSize;
		buffer = new byte[bufferSize];
		next = 0;
		last = 0;
		open(fileName);
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
		throw new IOException("Unimplemented method!");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	public void open(String fileName) throws IOException {
		this.fileName = fileName;
		try {
			randomAccFile = new RandomAccessFile(fileName, "r");
			latestpos = 0;
			next = last = 0;
		} catch (FileNotFoundException e) {
			throw new IOException(e);
		}
	}

	@Override
	public int read() throws IOException {
		return randomAccFile.read();
	}

	@Override
	public int read(char[] buff) throws IOException {
		throw new IOException("Unimplemented method!");
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		throw new IOException("Unimplemented method!");
	}

	@Override
	public int read(CharBuffer arg0) throws IOException {
		throw new IOException("Unimplemented method!");
	}

	@Override
	public String readLine() throws IOException {
		StringBuilder sb = null;
		while (true) {
			if (last <= next) {
				// Read buffer
				next = 0;
				latestpos = randomAccFile.getFilePointer();
				last = randomAccFile.read(buffer);

				// End of file?
				if (last < 0) return removeNewLine(sb);
			}

			// Find end of line
			int nl = findNl(next, last);
			if (sb == null) sb = new StringBuilder();
			if (nl >= 0) {
				sb.append(new String(buffer, next, nl - next));
				next = nl + 1;
				return removeNewLine(sb);
			} else {
				sb.append(new String(buffer, next, last - next));
				last = next;
			}
		}
	}

	@Override
	public boolean ready() throws IOException {
		throw new IOException("Unimplemented method!");
	}

	/**
	 * Remove trailing newline characters
	 */
	String removeNewLine(StringBuilder sb) {
		if (sb == null) return null;
		if (sb.length() <= 0) return "";

		// Remove trailing newlines
		for (char c = sb.charAt(sb.length() - 1); c == '\r' || c == '\n';) {
			sb.deleteCharAt(sb.length() - 1);

			if (sb.length() > 0) c = sb.charAt(sb.length() - 1);
			else break;
		}
		return sb.toString();
	}

	@Override
	public void reset() throws IOException {
		throw new IOException("Unimplemented method!");
	}

	/**
	 * Seek to a position in the file
	 */
	public void seek(long pos) throws IOException {
		randomAccFile.seek(pos);
		latestpos = pos;
		next = last = 0;
	}

	@Override
	public long skip(long n) throws IOException {
		throw new IOException("Unimplemented method!");
	}

	@Override
	public String toString() {
		return fileName + ":" + getFilePointer();
	}

}
