package org.snpeff.binseq.coder;

import org.snpeff.nmer.Nmer;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Class used to encode & decode sequences into binary and vice-versa
 *
 * Note:This is a singleton class.
 *
 * It stores DNA bases into 2 bits {a,c,g,t} <-> {0,1,2,3}
 *
 * @author pcingola
 *
 */
public class DnaCoder extends Coder {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static boolean debug = false;
	private static DnaCoder dnaCoder = new DnaCoder();

	protected static final int BITS_PER_BASE = 2;
	protected static final long MASK_FIRST_BASE = 3L;
	protected static final int BASES_PER_LONGWORD = BITS_PER_LONGWORD / BITS_PER_BASE;
	protected static final int LAST_BASE_IN_LONGWORD = BASES_PER_LONGWORD - 1;
	public static final long MASK_ALL_WORD = 0xffffffffffffffffL;
	public static final char[] TO_BASE = { 'a', 'c', 'g', 't' };

	public long MASK_BASE[], MASK_LOW[], MASK_HIGH[];
	public int COUNT_DIFFS[];

	public static DnaCoder get() {
		return dnaCoder;
	}

	/**
	 * Static class initialization
	 */
	DnaCoder() {
		// Initialize mask
		MASK_BASE = new long[DnaCoder.BASES_PER_LONGWORD];
		MASK_LOW = new long[Coder.BITS_PER_LONGWORD + 1];
		MASK_HIGH = new long[Coder.BITS_PER_LONGWORD + 1];

		// Initialize masks
		for (int i = 0; i < MASK_BASE.length; i++)
			MASK_BASE[i] = MASK_FIRST_BASE << (BITS_PER_BASE * i);

		long low = 1L, high = 1L << 63;
		MASK_LOW[0] = MASK_HIGH[0] = 0;
		for (int i = 1; i < MASK_LOW.length; i++) {
			int j = i - 1;
			MASK_LOW[i] = low = (1L << j) | low;
			MASK_HIGH[i] = high >> j; // Note: signed rotation
		}

		// Count diffs
		int countDiffLen = 16; // 16 bits
		COUNT_DIFFS = new int[1 << (countDiffLen - 1) + 1];
		for (int i = 1; i < COUNT_DIFFS.length; i++) {
			int count = 0;
			for (int j = 0; j <= countDiffLen; j += 2) {
				if (((i >> j) & 0x3) != 0) count++;
			}
			COUNT_DIFFS[i] = count;
		}
	}

	@Override
	public int basesPerWord() {
		return BASES_PER_LONGWORD;
	}

	/**
	 * Encode a base using 2 bits
	 * @param c
	 * @return
	 */
	@Override
	public int baseToBits(char c) {
		return baseToBits(c, false);
	}

	public int baseToBits(char c, boolean ignoreErrors) {
		switch (c) {
		case 'a':
		case 'A':
			return 0;
		case 'c':
		case 'C':
			return 1;
		case 'g':
		case 'G':
			return 2;
		case 't':
		case 'T':
		case 'u':
		case 'U':
			return 3;
		default:
			if (ignoreErrors) return 0;
			throw new RuntimeException("Unknown base '" + c + "'");
		}
	}

	@Override
	public int bitsPerBase() {
		return BITS_PER_BASE;
	}

	/**
	 * Copy 'length' bases from 'src' (starting from 'srcStart') to 'dst' (starting from 'dstStart')
	 * @param src
	 * @param srcStart
	 * @param dst
	 * @param length
	 */
	public void copyBases(long[] src, int srcStart, long[] dst, int dstStart, int length) {
		// Src parameters
		int startWord = srcStart / BASES_PER_LONGWORD;
		int startBase = srcStart % BASES_PER_LONGWORD;
		long startMask = MASK_LOW[BITS_PER_LONGWORD - startBase * BITS_PER_BASE];

		// Dst parameters
		int startWordDst = dstStart / BASES_PER_LONGWORD;
		int startBaseDst = dstStart % BASES_PER_LONGWORD;
		int endWordDst = (dstStart + length - 1) / BASES_PER_LONGWORD;
		int endBaseDst = (startBaseDst + length) % BASES_PER_LONGWORD;
		long startMaskDst = MASK_LOW[BITS_PER_LONGWORD - startBaseDst * BITS_PER_BASE];
		long endMaskDst = endBaseDst != 0 ? MASK_HIGH[endBaseDst * BITS_PER_BASE] : MASK_ALL_WORD;;

		// Rotation values
		int rolLsrc = BITS_PER_BASE * startBase;
		int rorHsrc = BITS_PER_LONGWORD - rolLsrc;
		int rorLdst = startBaseDst * BITS_PER_BASE;
		int rorHdst = BITS_PER_LONGWORD - rorLdst;

		// Copy words from src[] to dst[]
		long srcL = 0, srcH = 0, s, mask;
		for (int i = startWord, j = startWordDst, k = 0; k < length; i++, j++, k += 32) {
			//---
			// Get a 'long' from src[] (64 bits)
			//---
			srcL = (src[i] & startMask) << rolLsrc; // Get some bits from this word
			if ((startBase != 0) && ((i + 1) < src.length)) srcH = src[i + 1] >>> rorHsrc; // Do I need more bits from the next word? Is there a next word?
			else srcH = 0;
			s = srcH | srcL; // Store the 64 bits in one long

			//---
			// Set bits in dst[]
			//---
			if (j < dst.length) {
				// Mask: Which bits of 'dst' are kept
				if (j == endWordDst) mask = ~startMaskDst | ~endMaskDst;
				else mask = ~startMaskDst;

				// Set lower bits in dst[j]
				dst[j] = (dst[j] & mask) | ((s >>> rorLdst) & ~mask);

				// Do we need to set higher bits in dst[j+1] ?
				if ((j + 1) <= endWordDst) {
					if ((j + 1) == endWordDst) mask = startMaskDst | ~endMaskDst;
					else mask = startMaskDst;

					dst[j + 1] = (dst[j + 1] & mask) | ((s << rorHdst) & ~mask); // Set higher bits in dst[j+1]
				}
			}
		}
	}

	/**
	 * Copy 'length' bases from 'src' to 'dst' (starting from 'start')
	 * @param src
	 * @param start
	 * @param dst
	 * @param length
	 */
	public void copyBases(long[] src, long[] dst, int start, int length) {
		int startWord = start / BASES_PER_LONGWORD;
		int startBase = start % BASES_PER_LONGWORD;

		int endWord = (start + length) / BASES_PER_LONGWORD;
		int endBase = (startBase + length) % BASES_PER_LONGWORD;

		int len = (startBase + length) / BASES_PER_LONGWORD;

		long startMask = MASK_LOW[BITS_PER_LONGWORD - startBase * BITS_PER_BASE];
		long endMask = MASK_HIGH[endBase * BITS_PER_BASE];

		if (len == 0) {
			// Only one word to copy (start=end)
			long dstMaskedOut = dst[startWord] & (~(startMask & endMask)); // We have to keep the rest of the sequence intact
			dst[startWord] = dstMaskedOut | (src[startWord] & startMask & endMask);
		} else if (len == 1) {
			// Only two words to copy (start and end)
			long dstMaskedOut = dst[startWord] & (~startMask);// We have to keep the rest of the sequence intact
			dst[startWord] = dstMaskedOut | (src[startWord] & startMask);

			dstMaskedOut = dst[endWord] & (~endMask);// We have to keep the rest of the sequence intact
			dst[endWord] = dstMaskedOut | (src[endWord] & endMask);
		} else {
			// Several words to copy (start, end, the rest)
			long dstMaskedOut = dst[startWord] & (~startMask);// We have to keep the rest of the sequence intact
			dst[startWord] = dstMaskedOut | (src[startWord] & startMask);

			dstMaskedOut = dst[endWord] & (~endMask);// We have to keep the rest of the sequence intact
			dst[endWord] = dstMaskedOut | (src[endWord] & endMask);

			System.arraycopy(src, startWord + 1, dst, startWord + 1, len - 1); // Use a fast copy method
		}
	}

	/**
	 * Count number of different bases after an XOR comparison (see score method)
	 * This method is optimized using 16-bits pre-computed counts (COUNT_DIFFS)
	 * @param compare
	 * @return
	 */
	int countDiffBases(long compare) {
		int comp = COUNT_DIFFS[(int) (compare & 0xffffL)];
		comp += COUNT_DIFFS[(int) ((compare >> 16) & 0xffffL)];
		comp += COUNT_DIFFS[(int) ((compare >> 32) & 0xffffL)];
		comp += COUNT_DIFFS[(int) ((compare >> 48) & 0xffffL)];
		return comp;
	}

	void d(String m, long l) {
		Nmer n = new Nmer(32);
		n.setNmer(l);
		Log.debug(String.format("%10s\t%s\t%s", m, Gpr.bin64(l), n.toString()));
	}

	/**
	 * Decode bits from a given position
	 * @param word
	 * @param pos
	 * @return
	 */
	@Override
	public int decodeWord(long word, int pos) {
		int c = (int) ((word & MASK_BASE[pos]) >>> (pos << 1));
		return c;
	}

	/**
	 * Encode a base to a given position in a word
	 * @param base
	 * @param pos
	 * @return
	 */
	public long encodeWord(char base, int pos) {
		return ((long) baseToBits(base)) << (pos << 1);
	}

	@Override
	public int lastBaseinWord() {
		return LAST_BASE_IN_LONGWORD;
	}

	/**
	 * Calculate the coded length of a sequence in 'words' (depends on coder)
	 * @param len
	 * @return
	 */
	public int length2words(int len) {
		return ((len % basesPerWord()) != 0 ? len / basesPerWord() + 1 : len / basesPerWord());
	}

	@Override
	public long mask(int baseIndexInWord) {
		return MASK_BASE[baseIndexInWord];
	}

	/**
	 * Decode bits from a given position
	 * @param code
	 * @param pos
	 * @return
	 */
	public long replaceBase(long code, int pos, char newBase) {
		return (code & (~MASK_BASE[pos])) | encodeWord(newBase, pos);
	}

	/**
	 * Reverse all bases in 'code'
	 * @param linearIndex
	 * @return
	 */
	public long reverseBases(long code) {
		long reversedCode = 0;
		for (int pos = 0; pos < basesPerWord(); pos++) {
			int c = decodeWord(code, pos);
			reversedCode = reversedCode << BITS_PER_BASE | c;
		}
		return reversedCode;
	}

	/**
	 * Calculate a 'score' for a sequence (dst) and a sub-sequence (src).
	 * The score is the number of equal bases (or zero if they differ)
	 *
	 * @param dst : Destination sequence codes[]
	 * @param src : Source sequence codes[]
	 * @param srcStart : Source sub-sequence start
	 * @param length : Number of bases to compare
	 * @param threshold: Number of bases allowed to differ
	 *
	 * @return
	 */
	public int score(long dst[], long src[], int srcStart, int length, int threshold) {
		// Src parameters
		int startWord = srcStart / BASES_PER_LONGWORD;
		int startBase = srcStart % BASES_PER_LONGWORD;
		long startMask = MASK_LOW[BITS_PER_LONGWORD - startBase * BITS_PER_BASE];

		// Dst parameters
		int endWordDst = (length - 1) / BASES_PER_LONGWORD;
		int endBaseDst = length % BASES_PER_LONGWORD;
		long endMaskDst = endBaseDst != 0 ? MASK_HIGH[endBaseDst * BITS_PER_BASE] : MASK_ALL_WORD;;

		// Rotation values
		int rolLsrc = BITS_PER_BASE * startBase;
		int rorHsrc = BITS_PER_LONGWORD - rolLsrc;

		// Copy words from src[] to dst[]
		long srcL = 0, srcH = 0, s, mask;
		int diffBases = 0;
		for (int i = startWord, j = 0, k = 0; k < length; i++, j++, k += 32) {
			//---
			// Get a 'long' from src[] (64 bits)
			//---
			srcL = (src[i] & startMask) << rolLsrc; // Get some bits from this word
			if ((startBase != 0) && ((i + 1) < src.length)) srcH = src[i + 1] >>> rorHsrc; // Do I need more bits from the next word? Is there a next word?
			else srcH = 0;
			s = srcH | srcL; // Store the 64 bits in one long

			//---
			// Compare bits in dst[]
			//---
			if (j < dst.length) {
				// Mask: Which bits of 'dst' are not compared
				if (j == endWordDst) mask = endMaskDst;
				else mask = MASK_ALL_WORD;

				// Compare lower bits in dst[j]
				long compare = (dst[j] ^ s) & mask;
				if (compare != 0) { // Any differences?
					// Should we continue?
					if (threshold == 0) return 0;
					else {
						diffBases += countDiffBases(compare);
						if (diffBases > threshold) return 0;
					}
				}
			}
		}

		return length - diffBases;
	}

	/**
	 * Decode a base using 2 bits
	 */
	@Override
	public char toBase(int code) {
		return TO_BASE[code];
	}

	@Override
	public char toBase(long word, int pos) {
		int c = (int) ((word & MASK_BASE[pos]) >>> (pos << 1));
		return toBase(c);
	}

}
