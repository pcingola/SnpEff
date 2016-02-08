package org.snpeff.fileIterator;

import java.io.IOException;
import java.io.Reader;

/**
 * A buffered reader for a file. It allows to 'seek' and 'readLine()'
 *  
 * @author pablocingolani
 */
public class NullReader extends Reader {

	public NullReader() {
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public int read(char[] arg0, int arg1, int arg2) throws IOException {
		return 0;
	}

}
