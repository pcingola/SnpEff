package org.snpeff.vcf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.snpeff.interval.Chromosome;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Index a file that has "chr \t pos" as the beginning of a line (e.g. VCF)
 *
 * WARNING: It is assumed that the file is ordered by position (chromosome order does not matter)
 *
 * @author pcingola
 */
public class FileIndexChrPos {

	/**
	 * A part of a file
	 *
	 */
	public class FileRegion {
		long start, end;
		String lineStart, lineEnd;

		@Override
		public String toString() {
			return start + "\t" + lineStart //
					+ "\n"//
					+ end + "\t" + lineEnd //"
			;
		}
	}

	/**
	 * A line and the position on the file where it begins
	 */
	public class LineAndPos {
		public String line; // Line
		public long position; // Position in file where line starts

		@Override
		public String toString() {
			String str = "";
			if (line != null) {
				if (line.length() > 50) str = line.substring(0, 49) + "...";
				else str = line;
			}
			return position + "\t" + str;
		}
	}

	public static final int POS_OFFSET = 1; // VCF files are one-based
	private static final int BUFF_SIZE = 17; // 1024 * 1024;

	boolean verbose = false;
	boolean debug = false;
	String fileName;
	long size = 0;
	RandomAccessFile file;
	HashMap<String, FileRegion> fileRegions = new HashMap<String, FileIndexChrPos.FileRegion>(); // Store file regions by chromosome

	public FileIndexChrPos(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Get chromosome info
	 */
	String chromo(String line) {
		if (line.startsWith("#")) return null;
		return line.split("\\t")[0];
	}

	/**
	 * Close file
	 */
	public void close() {
		try {
			if (file != null) file.close();
		} catch (IOException e) {
			System.err.println("I/O problem while closing file '" + fileName + "'");
			throw new RuntimeException(e);
		}
		file = null;
	}

	/**
	 * Dump a region of the file to STDOUT
	 *
	 * @param posStart : Start file coordinate
	 * @param posEnd   : End file coordinate
	 * @param toString : Return a sting with file contents?
	 *
	 * @return If toString is 'true', return a string with file's content between those coordinates (this is used only for test cases and debugging)
	 */
	String dump(long start, long end, boolean toString) {
		if (verbose) Log.info("\tDumping file '" + fileName + "' interval [ " + start + " , " + end + " ]");

		StringBuilder sb = new StringBuilder();

		try {
			byte buff[] = new byte[BUFF_SIZE];
			file.seek(start);
			for (long curr = start; curr <= end;) {
				long len = Math.min(BUFF_SIZE, end - curr + 1); // Maximum length to read
				int read = file.read(buff, 0, (int) len); // Read file

				if (read <= 0) break; // Error or nothing read, abort

				String out = new String(buff, 0, read);

				// Show or append to string
				if (toString) sb.append(out);
				else System.out.print(out);

				curr += read;
			}
		} catch (Exception e) {
			throw new RuntimeException("Error reading file '" + fileName + "' from position " + start + " to " + end);
		}

		return sb.toString();
	}

	/**
	 * Dump all lines in the interval chr:posStart-posEnd
	 *
	 * @param chr      : Chromosome
	 * @param posStart : Start coordinate in chromosome (zero-based)
	 * @param posEnd   : End coordinate in chromosome (zero-based)
	 * @param toString : Return a sting with file contents?
	 *
	 * @return If toString is 'true', return a string with file's content between those coordinates (this is used only for test cases and debugging)
	 */
	public String dump(String chr, int posStart, int posEnd, boolean toString) {
		long fileStart = find(chr, posStart, true);
		long fileEnd = find(chr, posEnd, false);

		return dump(fileStart, fileEnd - 1, toString);
	}

	/**
	 * Find the position in the file for the first character of the first line whose genomic position is less or equal than 'chrPos'
	 * @param chrPos : Chromosome coordinate (zero-based)
	 * @param start : File start coordinate (zero-based)
	 * @param lineStart : Line at 'start' coordinate
	 * @param end : File end coordinate (zero-based)
	 * @param lineEnd : Line at 'end' coordinate
	 * @return position in file between [start, end] where chrPos can be found
	 */
	long find(int chrPos, long start, String lineStart, long end, String lineEnd, boolean lessEq) {
		//---
		// Check break conditions
		//---
		int posStart = pos(lineStart);
		int posEnd = pos(lineEnd);
		if (debug) Log.debug("Find:\t" + chrPos + "\t[" + posStart + ", " + posEnd + "]\tFile: [" + start + " , " + end + "]\tsize: " + (end - start) //
				+ "\n\t\t\t\t" + s(lineStart) //
				+ "\n\t\t\t\t" + s(lineEnd) //
				+ "\n");

		if (chrPos == posStart) return found(start, lineStart, lessEq); // Is it lineStart?
		if (posEnd == chrPos) return found(end, lineEnd, lessEq); // Is it lineEnd?
		if (chrPos < posStart) return start; // Before start?
		if (posEnd < chrPos) return end + lineEnd.length() + 1; // After end?
		if (start + 1 >= end) { // Only one byte of difference between start an end? (i.e. we are at a line boundary)
			if (chrPos <= posStart) return found(start, lineStart, lessEq);
			if (chrPos < posEnd) return found(end, lineEnd, true);
			return found(end, lineEnd, false);
		}

		if (posStart >= posEnd) throw new RuntimeException("This should never happen! Is the file sorted by position?"); // Sanity check

		//---
		// Recurse
		//---
		long mid = (start + end) / 2;
		LineAndPos lpmid = getLine(mid);
		String lineMid = lpmid.line;
		long posMid = pos(lineMid);

		if (chrPos <= posMid) return find(chrPos, start, lineStart, mid, lineMid, lessEq);
		else return find(chrPos, mid, lineMid, end, lineEnd, lessEq);
	}

	/**
	 * Find the position in the file for the first character of the first line equal or less than a specific chr:pos
	 */
	public long find(String chr, int pos, boolean lessEq) {
		chr = Chromosome.simpleName(chr);
		FileRegion fr = fileRegions.get(chr);
		if (fr == null) throw new RuntimeException("No such chromosome: '" + chr + "'");

		// Find position in file
		long posFound = find(pos, fr.start, fr.lineStart, fr.end, fr.lineEnd, lessEq);

		// Get line information
		LineAndPos linePos = getLine(posFound);

		// Get beginning of line position
		if (linePos != null) return linePos.position;
		return -1;
	}

	/**
	 * Calculate coordinate of this line or next line
	 */
	long found(long filePos, String fileLine, boolean lessEq) {
		if (!lessEq) {
			long pos = filePos + fileLine.length() + 1; // Next line
			return (pos < size() ? pos : size());
		}

		// Beginning of 'filePos' line
		return filePos;
	}

	/**
	 * Get a byte from a file
	 */
	public byte get(long bytePosition) {
		try {
			// Change position if needed
			if (file.getFilePointer() != bytePosition) file.seek(bytePosition);
			return (byte) file.read();
		} catch (IOException e) {
			throw new RuntimeException("Error readin file '" + fileName + "' at position " + bytePosition, e);
		}
	}

	/**
	 * Read 'len' bytes after 'bytePosition' or until a '\n' is reached.
	 * If len is negative, read 'abs(len)' bytes before bytePosition or until '\n' is reached
	 *
	 * @param bytePosition
	 * @param len
	 * @return An array of 'len' bytes. null if either end of file (len > 0) or beginning of file (len < 0)
	 */
	public byte[] get(long bytePosition, int len) {
		try {
			int size = Math.abs(len);

			// Change position if needed
			long pos = bytePosition;
			if (len < 0) {
				if (bytePosition <= 0) return null;
				pos -= size;

				// Before beginning of file?
				if (pos < 0) {
					pos = 0;
					size = (int) bytePosition;
				}
				pos = Math.max(pos, 0);
			}
			if (file.getFilePointer() != pos) file.seek(pos);

			byte buff[] = new byte[size];
			int read = file.read(buff);

			// Nothing to read?
			if (read <= 0) return null;

			// Buffer was too long? Return an array of byte with exactly the number of bytes
			if (read < buff.length) {
				byte newBuff[] = new byte[read];
				System.arraycopy(buff, 0, newBuff, 0, read);
				buff = newBuff;
			}

			// Only return bytes until 'new line'
			if (len > 0) {
				// Find new line
				int newLine = -1;
				for (int i = 0; i < read; i++)
					if (buff[i] == '\n') {
						newLine = i;
						break;
					}

				// Copy only the part until a newLine
				if (newLine >= 0) {
					byte newBuff[] = new byte[newLine + 1];
					System.arraycopy(buff, 0, newBuff, 0, newLine + 1);
					buff = newBuff;
				}
			} else if (len < 0) {
				// Find new line backwards
				int newLine = -1;
				for (int i = read - 1; i >= 0; i--)
					if (buff[i] == '\n') {
						newLine = i;
						break;
					}

				// Copy only the part until a newLine
				if (newLine >= 0) {
					byte newBuff[] = new byte[read - newLine];
					for (int i = newLine, j = 0; i < read; i++, j++)
						newBuff[j] = buff[i];
					buff = newBuff;
				}
			}

			return buff;
		} catch (IOException e) {
			throw new RuntimeException("Error readin file '" + fileName + "' at position " + bytePosition, e);
		}
	}

	/**
	 * Available chromosomes
	 * @return
	 */
	public Set<String> getChromos() {
		return fileRegions.keySet();
	}

	/**
	 * Get position where 'chr' ends
	 * @param chr
	 * @return -1 if 'chr' is not in the index
	 */
	public long getEnd(String chr) {
		chr = Chromosome.simpleName(chr);
		FileRegion fr = fileRegions.get(chr);
		if (fr == null) return -1;
		return fr.end;
	}

	/**
	 * Get file region for a given chrosmome
	 */
	FileRegion getFileRegion(String chr) {
		chr = Chromosome.simpleName(chr);
		FileRegion fr = fileRegions.get(chr);
		if (fr == null) {
			fr = new FileRegion();
			fileRegions.put(chr, fr);
		}
		return fr;
	}

	/**
	 * Get the line where 'pos' hits
	 * @return A string with the line that 'pos' hits, null if it's out of boundaries
	 */
	public LineAndPos getLine(long pos) {
		int BUFF_SIZE = 10240;
		long size = size();
		if ((pos >= size) || (pos < 0)) return null;

		LineAndPos linePos = new LineAndPos();
		StringBuffer sb = new StringBuffer();

		// Get bytes after 'pos'
		long position;
		for (position = pos; position < size;) {
			byte b[] = get(position, BUFF_SIZE);
			if (b == null) break; // End of file
			sb.append(new String(b));
			if (b[b.length - 1] == '\n') break; // Found new line?
			position += b.length;
		}

		// Get bytes before 'pos'
		for (position = pos; position >= 0;) {
			byte b[] = get(position, -BUFF_SIZE);
			if (b == null) break; // Beginning of file
			sb.insert(0, new String(b));
			position -= b.length;
			if (b[0] == '\n') break; // Found new line?
		}

		// Remove leading and trailing '\n'?
		int lineStart = 0, lineEnd = sb.length();
		if (sb.charAt(0) == '\n') lineStart = 1;
		if (sb.charAt(sb.length() - 1) == '\n') lineEnd = sb.length() - 1;

		linePos.line = sb.toString().substring(lineStart, lineEnd);
		linePos.position = position + lineStart;

		return linePos;
	}

	/**
	 * A slow method for getLine
	 */
	public LineAndPos getLineSlow(long pos) {
		long size = size();
		if ((pos >= size) || (pos < 0)) return null;

		LineAndPos linePos = new LineAndPos();
		StringBuffer sb = new StringBuffer();

		// Get bytes before 'pos'
		long position;
		for (position = pos - 1; position >= 0; position--) {
			byte b = get(position);
			if (b == '\n') break;
			sb.insert(0, (char) b);
		}
		linePos.position = position + 1;

		// Get bytes after 'pos'
		for (position = pos; position < size; position++) {
			byte b = get(position);
			if (b == '\n') break;
			sb.append((char) b);
		}
		linePos.line = sb.toString();

		// if (debug) Log.debug("Line & Position: " + linePos);
		return linePos;
	}

	/**
	 * Get position where 'chr' starts
	 * @param chr
	 * @return -1 if 'chr' is not in the index
	 */
	public long getStart(String chr) {
		chr = Chromosome.simpleName(chr);
		FileRegion fr = fileRegions.get(chr);
		if (fr == null) return -1;
		return fr.start;
	}

	/**
	 * Index chromosomes in the whole file
	 */
	public void index() {
		if (file == null) throw new RuntimeException("File error (forgot to open the file?).");

		// Last line (minus '\n' character, minus one)
		long end = size() - 1;
		if (end < 0) return; // Empty database file

		String lineEnd = getLine(end).line;
		String chrEnd = chromo(lineEnd);

		// Add fileRegion.end for last chromsome in the file
		FileRegion fr = getFileRegion(chrEnd);
		fr.end = end;
		fr.lineEnd = lineEnd;
		if (verbose) Log.info("\tindex:\t" + chrEnd + "\t" + end);

		// Find first non-comment line
		long start = 0;
		String lineStart = "";
		for (start = 0; start < size; start += lineStart.length() + 1) {
			lineStart = getLine(start).line;
			if (chromo(lineStart) != null) break;
		}

		String chrStart = chromo(lineStart);

		// Add fileRegion.start for first chromsome in the file
		fr = getFileRegion(chrStart);
		fr.start = start;
		fr.lineStart = lineStart;
		if (verbose) Log.info("\tindex:\t" + chrStart + "\t" + start);

		// Index the rest of the file
		indexChromos(start, lineStart, end, lineEnd);
	}

	/**
	 * Index chromosomes in a region of a file
	 * @param start
	 * @param lineStart
	 * @param end
	 * @param lineEnd
	 */
	void indexChromos(long start, String lineStart, long end, String lineEnd) {
		if (debug) Log.debug("Index:"//
				+ "\n\t" + start + "(" + (((double) start) / size()) + ") :\t" + s(lineStart) //
				+ "\n\t" + end + "(" + (((double) end) / size()) + ") :\t" + s(lineEnd));

		if (start > end) throw new RuntimeException("This should never happen! Start: " + start + "\tEnd: " + end);

		// Parse chromosome start and end
		String chrStart = chromo(lineStart);
		if (chrStart == null) throw new RuntimeException("Cannot extract chromosome data from line:"//
				+ "\n\tPosition : " + start + " (byte position in file) "//
				+ "\n\tLine     : " + lineStart//
		);

		String chrEnd = chromo(lineEnd);
		if (chrEnd == null) throw new RuntimeException("Cannot extract chromosome data from line:"//
				+ "\n\tPosition : " + end + " (byte position in file) "//
				+ "\n\tLine     : " + lineEnd//
		);

		if (chrStart.equals(chrEnd)) {
			if (debug) Log.debug("Chromo:\tlineStart: " + chrStart + "\tlineEnd: " + chrEnd + "\t==> Back!");
			return;
		}
		if (debug) Log.debug("Chromo:\tlineStart: " + chrStart + "\tlineEnd: " + chrEnd);

		if ((start + lineStart.length() + 1) >= end) {
			if (verbose) Log.info("\t\t" + chrStart + " / " + chrEnd + "\t" + start + " / " + end);

			// Add index where chromosome starts
			getFileRegion(chrEnd).start = getLine(end).position;
			getFileRegion(chrEnd).lineStart = lineEnd;

			// Add index where chromosome ends
			getFileRegion(chrStart).end = getLine(start).position;
			getFileRegion(chrStart).lineEnd = lineStart;
			return;
		}

		long mid = (start + end) / 2;
		String lineMid = getLine(mid).line;
		if (debug) Log.debug("Mid: " + mid + "\t" + s(lineMid));

		if (debug) Log.debug("First half recustion:");
		indexChromos(start, lineStart, mid, lineMid);

		if (debug) Log.debug("Second half recustion:");
		indexChromos(mid, lineMid, end, lineEnd);
	}

	void init(FileChannel channel) throws IOException {
	}

	/**
	 * Open file and initiate mappings
	 */
	public void open() {
		try {
			File f = new File(fileName);
			size = f.length();
			file = new RandomAccessFile(f, "r");
		} catch (FileNotFoundException e) {
			System.err.println("File not found '" + fileName + "'");
			throw new RuntimeException(e);
		}
	}

	/**
	 * The position argument of a line (second column in tab-separated format). Negative if not found
	 * @return The position argument of a line. Negative if not found
	 */
	public int pos(String line) {
		if (line.startsWith("#")) return -1; // In VCF, positions are one-based, so zero denotes an error
		return Gpr.parseIntSafe(line.split("\\t")[1]) - POS_OFFSET;
	}

	String s(String s) {
		if (s == null) return "null";
		return s.length() <= 50 ? s : s.substring(0, 50) + "...";
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * File size
	 */
	public long size() {
		return size;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		ArrayList<String> keys = new ArrayList<>();
		keys.addAll(fileRegions.keySet());
		Collections.sort(keys);

		for (String key : keys)
			sb.append(key + ":\n" + Gpr.prependEachLine("\t\t", fileRegions.get(key).toString()) + "\n");

		return sb.toString();
	}
}
