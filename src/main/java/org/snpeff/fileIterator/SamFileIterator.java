package org.snpeff.fileIterator;

import java.io.IOException;
import java.util.Iterator;

import org.snpeff.sam.SamEntry;
import org.snpeff.sam.SamHeader;

/**
 * Reads a SAM file
 * Note: This is a very 'rustic' reader (we should use Picard's API instead)
 * 
 * @author pcingola
 */
public class SamFileIterator extends FileIterator<SamEntry> {

	public static boolean debug = false;
	SamHeader headers;

	public SamFileIterator(String samFileName) {
		super(samFileName);
		headers = new SamHeader();
	}

	public SamHeader getHeaders() {
		return headers;
	}

	@Override
	public Iterator<SamEntry> iterator() {
		return this;
	}

	/**
	 * Read a sequence from the file
	 * @return
	 */
	@Override
	protected SamEntry readNext() {
		try {
			while((line = reader.readLine()) != null) {
				lineNum++;
				if( line.startsWith("@") ) headers.addHeaderRecord(line);
				else return new SamEntry(line);
			}
		} catch(IOException e) {
			return null;
		}

		return null;
	}
}
