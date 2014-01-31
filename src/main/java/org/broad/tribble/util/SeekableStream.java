package org.broad.tribble.util;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;

/**
 * User: jrobinso
 * Date: Nov 29, 2009
 */
public abstract class SeekableStream extends InputStream {

    public abstract void seek(long position) throws IOException;

    public abstract long position() throws IOException;

    /**
     * Read enough bytes to fill the input buffer
     */
    public void readFully(byte b[]) throws IOException {
        int len = b.length;
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = read(b, n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }

    public abstract boolean eof() throws IOException;


    public abstract long length();
}
