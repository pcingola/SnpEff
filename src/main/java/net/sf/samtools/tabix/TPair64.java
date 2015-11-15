package net.sf.samtools.tabix;

import java.io.IOException;
import java.io.InputStream;

import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Pair of 'long' (64 bits)
 */
public class TPair64 implements Comparable<TPair64> {

	long u, v;

	/**
	 * Unsigned 64-bit comparison
	 */
	public static boolean less64(final long u, final long v) {
		return (u < v) ^ (u < 0) ^ (v < 0);
	}

	public TPair64() {
	}

	public TPair64(final long _u, final long _v) {
		u = _u;
		v = _v;
	}

	public TPair64(final TPair64 p) {
		u = p.u;
		v = p.v;
	}

	@Override
	public int compareTo(final TPair64 p) {
		return u == p.u ? 0 : ((u < p.u) ^ (u < 0) ^ (p.u < 0)) ? -1 : 1; // unsigned 64-bit comparison
	}

	public void readIndex(InputStream is) throws IOException {
		u = TabixReader.readLong(is);
		v = TabixReader.readLong(is);
	}

	@Override
	public String toString() {
		long u16 = u >> 16;
		long v16 = v >> 16;
		return "<" + u + "," + v + "> = < " + u16 + " , " + v16 + " >, size: " + Gpr.toByteSize(Math.abs(v16 - u16));
	}

	public String toStringRor16() {
		return (u >> 16) + "\t" + (v >> 16);
	}

}
