package org.snpeff.nmer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.snpeff.binseq.coder.Coder;
import org.snpeff.binseq.coder.DnaCoder;

/**
 * Binary packed N-mer (i.e. DNA sequence of length N)
 * In this implementation N has to be less than 32
 * 
 * @author pcingola
 *
 */
public class Nmer {

	public static final int MAX_NMER_SIZE = 32;

	static long MASK[];
	private static final Coder coder = DnaCoder.get();

	int length;
	long nmer;

	static {
		MASK = new long[coder.basesPerWord() + 1];
		long mask = 0;
		for( int i = 0; i < MASK.length; i++ ) {
			MASK[i] = mask;
			mask |= mask << 2 | 3;
		}
	}

	public Nmer(int length) {
		setLength(length);
		nmer = 0;
	}

	public Nmer(String nmer) {
		set(nmer);
	}

	/**
	 * Set nmer (binary coded)
	 * @param nmer
	 */
	public long getNmer() {
		return nmer;
	}

	@Override
	public int hashCode() {
		int low = (int) (nmer & 0xffffffffL);
		int high = (int) ((nmer & 0xffffffff00000000L) >> 32);
		return high * 33 + low;
	}

	/**
	 * Get nmer's length
	 * @return
	 */
	public int length() {
		return length;
	}

	public int read(InputStream outputStream) throws IOException {
		long l = 0;
		int in = 0;
		for( int i = 0; i < 8; i++ ) {
			in = outputStream.read();
			if( in < 0 ) return in;
			l = (l << 8) | (in & 0xff);
		}
		nmer = l;
		return in;
	}

	/**
	 * Rotate sequence left and append a base at the end
	 * Note: The first base is dropped, so the total length is still 'n'
	 * @param cq
	 */
	public void rol(char base) {
		nmer <<= 2;
		nmer = MASK[length] & (nmer | coder.baseToBits(base));
	}

	/**
	 * Set nmer's sequence
	 * @param seqStr
	 */
	public void set(String seqStr) {
		if( seqStr == null ) {
			length = 0;
			nmer = 0;
		} else {
			nmer = 0;
			setLength(seqStr.length());

			// Create binary sequence
			char seqChar[] = seqStr.toCharArray();
			int i;
			for( i = 0; i < length; i++ )
				rol(seqChar[i]);
		}
	}

	/**
	 * Set nmer's length
	 * @param length
	 */
	public void setLength(int length) {
		if( (length < 1) || (length > MAX_NMER_SIZE) ) throw new RuntimeException("Nmer max length must be between 1 and " + coder.basesPerWord());
		this.length = length;
	}

	/**
	 * Set nmer (binary coded)
	 * @param nmer
	 */
	public void setNmer(long nmer) {
		this.nmer = nmer;
	}

	@Override
	public String toString() {
		char chars[] = new char[length];
		for( int i = length - 1, j = 0; i >= 0; i--, j++ )
			chars[j] = coder.toBase(coder.decodeWord(nmer, i));
		return new String(chars);
	}

	/**
	 * Watson-Cricks complement of this nmer
	 * 
	 * Since {A, C, G, T} is translated into binary {00, 01, 10, 11}. The complement is: 
	 *     baseWc = XOR(11, base)
	 *      
	 * @return
	 */
	public long wc() {
		return MASK[length] & (0xffffffffffffffffL ^ nmer); // I could have written it as "-1L ^ nmer", but it is a little bit more cryptic
	}

	public void write(OutputStream outputStream) throws IOException {
		for( int i = 56; i >= 0; i -= 8 ) {
			outputStream.write((int) (nmer >>> i));
		}
	}

}
