package org.snpeff.binseq.coder;

import java.io.Serializable;

/**
 * Class used to encode & decode sequences into binary and vice-versa
 * They are usually stored in 'long' words
 * 
 * @author pcingola
 */
public abstract class Coder implements Serializable {

	public static final int BITS_PER_LONGWORD = 64;
	public static final int BYTES_PER_LONGWORD = 8;

	/**
	 * How many bases can we pack in a word
	 * @return
	 */
	public abstract int basesPerWord();

	/**
	 * Encode a base using a binary representation
	 * @param c
	 * @return
	 */
	public abstract int baseToBits(char c);

	/**
	 * How many bits do we need for each base
	 * @return
	 */
	public abstract int bitsPerBase();

	/**
	 * Decode bits from a given position in a word
	 * @param word
	 * @param pos
	 * @return
	 */
	public abstract int decodeWord(long word, int pos);

	/**
	 * Index of the last base coded in a word
	 * @return
	 */
	public abstract int lastBaseinWord();

	/**
	 * Bitmask for a base in a word
	 * @return
	 */
	public abstract long mask(int baseIndexInWord);

	/**
	 * Encode a quality using a binary representation
	 * @param c
	 * @return
	 */
	public int qualityToBits(int q) {
		return 0;
	}

	/**
	 * Decode a base using a binary representation
	 * @param c
	 * @return
	 */
	public abstract char toBase(int code);

	/**
	 * Decode a base from a given position in a word
	 * @param word
	 * @param pos
	 * @return
	 */
	public abstract char toBase(long word, int pos);

	/**
	 * Decode a quality
	 * @param c
	 * @return
	 */
	public int toQuality(int code) {
		return 0;
	}
}
