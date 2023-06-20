package org.snpeff.binseq.coder;

/**
 * Class used to encode & decode sequences into binary and vice-versa
 * 
 * 
 * 	- Every base is encoded in 8 bits:
 * 		- Six bits for the base quality [0 , .. , 63]
 * 		- Two bits for the base {a, c, g, t} <=> {0, 1, 2, 3}
 * 
 * @author pcingola
 *
 */
public class DnaQualityCoder extends DnaCoder {

	public static final int MAX_QUALITY = 63;
	public static final int MIN_QUALITY = 0;

	private static DnaQualityCoder dnaQualityCoder = new DnaQualityCoder();

	public static DnaQualityCoder get() {
		return dnaQualityCoder;
	}

	/**
	 * Static class initialization
	 */
	private DnaQualityCoder() {}

	@Override
	public int basesPerWord() {
		return 1;
	}

	public int baseToBits(char c, int quality) {
		return qualityToBits(quality) | baseToBits(c, false);
	}

	/**
	 * WARNING: This implementation transforms the quality to a range [0, ..., 63] (in order to use 6 bits)
	 */
	@Override
	public int qualityToBits(int quality) {
		return 0xff & (Math.max(Math.min(quality, MAX_QUALITY), MIN_QUALITY) << 2);
	}

	/**
	 * Replace quality value in a given code
	 * @param code
	 * @param newQuality
	 * @return
	 */
	public int replaceQuality(byte code, int newQuality) {
		return (code & 0x03) | qualityToBits(newQuality);
	}

	/**
	 * Decode a base using 2 bits
	 * @param c
	 * @return
	 */
	@Override
	public char toBase(int code) {
		return TO_BASE[code & 0x03];
	}

	@Override
	public int toQuality(int code) {
		return ((code & 0xff)) >> bitsPerBase();
	}
}
