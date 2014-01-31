/*
* Adapted from example code in
* Title: Hardcore Java
* Title: Java I/O
* Second Edition: May 2006
* ISBN 10: 0-596-52750-0
* ISBN 13: 9780596527501
*
* http://www.javafaq.nu/java-example-code-1078.html
*
*/
package org.broad.tribble.util;

import java.io.*;


public class LittleEndianInputStream extends FilterInputStream {

    public LittleEndianInputStream(InputStream in) {
        super(in);
    }

    public boolean readBoolean() throws IOException {
        int bool = in.read();
        if (bool == -1) throw new EOFException();
        return (bool != 0);
    }

    public byte readByte() throws IOException {
        int temp = in.read();
        if (temp == -1) throw new EOFException();
        return (byte) temp;
    }

    public int readUnsignedByte() throws IOException {
        int temp = in.read();
        if (temp == -1) throw new EOFException();
        return temp;
    }

    public short readShort() throws IOException {
        int byte1 = in.read();
        int byte2 = in.read();
        // only need to test last byte read
        // if byte1 is -1 so is byte2
        if (byte2 == -1) throw new EOFException();
        return (short) (((byte2 << 24) >>> 16) + ((byte1 << 24) >>> 24));
    }

    public int readUnsignedShort() throws IOException {
        int byte1 = in.read();
        int byte2 = in.read();
        if (byte2 == -1) throw new EOFException();
        return ((byte2 << 24) >> 16) + ((byte1 << 24) >> 24);
    }

    public char readChar() throws IOException {
        int byte1 = in.read();
        int byte2 = in.read();
        if (byte2 == -1) throw new EOFException();
        return (char) (((byte2 << 24) >>> 16) + ((byte1 << 24) >>> 24));
    }

    public int readInt() throws IOException {

        int byte1 = in.read();
        int byte2 = in.read();
        int byte3 = in.read();
        int byte4 = in.read();
        if (byte4 == -1) {
            throw new EOFException();
        }
        return (byte4 << 24)
                + ((byte3 << 24) >>> 8)
                + ((byte2 << 24) >>> 16)
                + ((byte1 << 24) >>> 24);

    }

    public long readLong() throws IOException {

        long byte1 = in.read();
        long byte2 = in.read();
        long byte3 = in.read();
        long byte4 = in.read();
        long byte5 = in.read();
        long byte6 = in.read();
        long byte7 = in.read();
        long byte8 = in.read();
        if (byte8 == -1) {
            throw new EOFException();
        }
        return (byte8 << 56)
                + ((byte7 << 56) >>> 8)
                + ((byte6 << 56) >>> 16)
                + ((byte5 << 56) >>> 24)
                + ((byte4 << 56) >>> 32)
                + ((byte3 << 56) >>> 40)
                + ((byte2 << 56) >>> 48)
                + ((byte1 << 56) >>> 56);

    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(this.readLong());
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(this.readInt());
    }

    /**
     * Read a null terminated byte array and return result as a string
     *
     * @return
     * @throws IOException
     */

    public String readString() throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream(100);
        byte b ;
        while ((b = (byte) in.read()) != 0) {
            bis.write(b);
        }
        return new String(bis.toByteArray());
    }

}
