package org.snpeff.fileIterator;

import java.io.IOException;
import java.util.Iterator;

import org.snpeff.util.Gpr;

/**
 * Iterate on each line in this file
 * 
 * @author pcingola
 */
public class LineFileIterator extends FileIterator<String> {

	public static boolean debug = false;

	public LineFileIterator(String lineSeqFileName) {
		super(lineSeqFileName);
		line = null;
		lineNum = 0;
		reader = null;
		reader = Gpr.reader(lineSeqFileName);
	}

	public LineFileIterator(String lineSeqFileName, boolean gzip) {
		super(lineSeqFileName);
		line = null;
		lineNum = 0;
		reader = null;
		reader = Gpr.reader(lineSeqFileName, gzip);
	}

	@Override
	public Iterator<String> iterator() {
		return this;
	}

	/**
	 * Read a sequence from the file
	 * @return
	 */
	@Override
	protected String readNext() {
		try {
			if (reader.ready()) {
				line = reader.readLine(); // Read a line (only if needed)
				if (line != null) {
					lineNum++;
					return line;
				}
			}
		} catch (IOException e) {
			return null;
		}

		return null;
	}
}
