package org.snpeff.binseq;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.snpeff.binseq.coder.Coder;
import org.snpeff.binseq.coder.DnaCoder;

/**
 * Binary packed DNA sequence
 *
 * Notes:
 * 		- This is designed for short sequences (such as "short reads")
 * 		- Every base is encoded in 2 bits {a, c, g, t} <=> {0, 1, 2, 3}
 * 		- All bits are stored in an array of 'words' (integers)
 * 		- Most significant bits are the first bases in the sequence (makes comparison easier)
 *
 * @author pcingola
 *
 */
public class DnaSequence extends BinarySequence {

	private static final long serialVersionUID = 4523047339848048494L;
	private static DnaSequence EMPTY = null;

	DnaCoder coder = DnaCoder.get();
	int length;
	long codes[];

	/**
	 * Empty sequence singleton
	 */
	public static DnaSequence empty() {
		if (EMPTY == null) EMPTY = new DnaSequence("");
		return EMPTY;
	}

	public DnaSequence(int length, long codes[]) {
		this.length = length;
		this.codes = codes;
	}

	public DnaSequence(String seqStr) {
		if (seqStr != null) set(seqStr);
	}

	public DnaSequence(String seqStr, boolean ignoreErrors) {
		if (seqStr != null) set(seqStr, ignoreErrors);
	}

	@Override
	public DnaSequence clone() {
		DnaSequence clone = (DnaSequence) super.clone();
		return clone;
	}

	@Override
	public int compareTo(BinarySequence o) {
		DnaSequence bs = (DnaSequence) o;
		int minlen = Math.min(length, bs.length);
		for (int i = 0; i < minlen; i++) {
			if (codes[i] < bs.codes[i]) return -1;
			if (codes[i] > bs.codes[i]) return 1;
		}
		return 0;
	}

	/**
	 * Create a new sequence
	 */
	protected DnaSequence factory() {
		return new DnaSequence(null);
	}

	/**
	 * Create a new sequence
	 * @return
	 */
	DnaSequence factory(int length, long codes[]) {
		return new DnaSequence(length, codes);
	}

	@Override
	public char getBase(int index) {
		// This is exactly the same as "getCoder().toBase(getCode(index));"
		int idx = index / coder.basesPerWord();
		int pos = coder.lastBaseinWord() - (index % coder.basesPerWord());
		int code = (int) ((codes[idx] & coder.MASK_BASE[pos]) >>> (pos << 1));
		return coder.toBase(code);
	}

	/**
	 * Get a few bases from this sequence
	 */
	public String getBases(int index, int len) {
		char bases[] = new char[len];
		int j = index / coder.basesPerWord();
		int k = coder.lastBaseinWord() - (index % coder.basesPerWord());
		for (int i = 0; i < len; i++) {
			bases[i] = coder.toBase(codes[j], k);
			k--;
			if (k < 0) {
				k = coder.lastBaseinWord();
				j++;
			}
		}
		return new String(bases);
	}

	/**
	 * Return the base at position 'index'
	 */
	@Override
	public int getCode(int index) {
		if ((index < 0) || (index > length)) throw new IndexOutOfBoundsException("Index requested " + index + ", sequence length is " + length);
		int idx = index / coder.basesPerWord();
		int off = coder.lastBaseinWord() - (index % coder.basesPerWord());
		return coder.decodeWord(codes[idx], off);
	}

	@Override
	public Coder getCoder() {
		return coder;
	}

	public long[] getCodes() {
		return codes;
	}

	@Override
	public String getSequence() {
		return getBases(0, length);
	}

	@Override
	public int hashCode() {
		long hash = 0;
		for (int i = 0; i < codes.length; i++)
			hash = hash * 33 + codes[i];

		return (int) hash;
	}

	/**
	 * Is this sequence empty?
	 * @return
	 */
	public boolean isEmpty() {
		return length <= 0;
	}

	/**
	 * Sequence lenth
	 * @return
	 */
	@Override
	public int length() {
		return length;
	}

	/**
	 * Creates a new sequence by overlapping 'this' and 'sequence'
	 *
	 * E.g.
	 * this.sequence  :  |xxxxxxxxxxxxxxxxxxxxOOOOOOOOOOOOOOOOOOOOOOOO                 |
	 * other.sequence :  |                    OOOOOOOOOOOOOOOOOOOOOOOOyyyyyyyyyyyyyyyyy|
	 *                   |                    |start=20                                |
	 * result         :  |xxxxxxxxxxxxxxxxxxxxOOOOOOOOOOOOOOOOOOOOOOOOyyyyyyyyyyyyyyyyy|
	 *
	 * this.sequence  :  |                        OOOOOOOOOOOOOOOOOOOOOOOOxxxxxxxxxxxxxxxxxxxx|
	 * other.sequence :  |yyyyyyyyyyyyyyyyyyyyyyyyOOOOOOOOOOOOOOOOOOOOOOOO                    |
	 *                   |start=-20                                                           |
	 * result         :  |yyyyyyyyyyyyyyyyyyyyyyyyOOOOOOOOOOOOOOOOOOOOOOOOxxxxxxxxxxxxxxxxxxxx|
	 *
	 * Another case is when a sequence is fully included in the other sequence. In this case the result is just a
	 * copy of the longest sequence (with the quality updated)
	 *
	 * E.g.
	 * this.sequence  :  |xxxxxxxxxxxxxxxxxxxxOOOOOOOOOOOOOOOzzzzzzzzz|
	 * other.sequence :  |                    OOOOOOOOOOOOOOO         |
	 *                   |                    |start=20               |
	 * result         :  |xxxxxxxxxxxxxxxxxxxxOOOOOOOOOOOOOOOzzzzzzzzz|
	 *
	 * this.sequence  :  |                        OOOOOOOOOOO             |
	 * other.sequence :  |yyyyyyyyyyyyyyyyyyyyyyyyOOOOOOOOOOOzzzzzzzzzzzzz|
	 *                   |start=-20                                       |
	 * result         :  |yyyyyyyyyyyyyyyyyyyyyyyyOOOOOOOOOOOzzzzzzzzzzzzz|
	 *
	 *
	 * @param start
	 * @return A new sequence
	 */
	@Override
	public BinarySequence overlap(BinarySequence sequence, int start) {
		DnaSequence seq = (DnaSequence) sequence;

		// Join both sequences
		long[] newCodes = null;
		int newLen = 0;
		int len = length();
		if (start >= 0) {
			newLen = start + sequence.length();
			int newLenWords = newLen / coder.basesPerWord();
			if (newLen % coder.basesPerWord() != 0) newLenWords++;

			if (len >= newLen) {
				newLen = len;
				newCodes = new long[codes.length]; // 'sequence' is totally included in 'this' => Just create a copy
				System.arraycopy(codes, 0, newCodes, 0, codes.length);
			} else {
				newCodes = new long[newLenWords];
				int lenWords = len / coder.basesPerWord();
				if ((len % coder.basesPerWord()) > 0) lenWords++;
				System.arraycopy(codes, 0, newCodes, 0, lenWords);
				coder.copyBases(seq.codes, len - start, newCodes, len, newLen - len);
			}
		} else {
			newLen = Math.max(-start + len, seq.length());
			int newLenWords = newLen / coder.basesPerWord();
			if (newLen % coder.basesPerWord() != 0) newLenWords++;
			newCodes = new long[newLenWords];
			System.arraycopy(seq.codes, 0, newCodes, 0, seq.codes.length);
			coder.copyBases(codes, 0, newCodes, -start, length);
		}

		DnaSequence newSeq = factory(newLen, newCodes); // Create a new sequence
		return newSeq;
	}

	/**
	 * Read data in binary format
	 */
	@Override
	public BinarySequence read(DataInputStream dataInStream) throws IOException {
		DnaSequence binSeq = factory();
		try {
			binSeq.readDataStream(dataInStream);
		} catch (EOFException e) {
			return null;
		}
		return binSeq;
	}

	/**
	 * Read data in binary format
	 */
	@Override
	protected void readDataStream(DataInputStream dataInStream) throws IOException {
		length = dataInStream.readInt();
		int ilen = coder.length2words(length);
		codes = new long[ilen];
		for (int i = 0; i < codes.length; i++)
			codes[i] = dataInStream.readLong();
	}

	@Override
	public BinarySequence reverseWc() {
		DnaSequence rwc = factory();
		rwc.codes = new long[codes.length];
		rwc.length = length;

		// Reverse all words and perform WC
		int j = 0, k = 0;
		long s = 0;
		for (int index = length - 1; index >= 0; index--) {
			int idx = index / coder.basesPerWord();
			int off = coder.lastBaseinWord() - (index % coder.basesPerWord());
			int c = coder.decodeWord(codes[idx], off);

			c = 0x03 & ~c; // WC complement

			// This is almost the same as  set() method
			s <<= 2;
			s |= c;
			k++;
			if (k >= coder.basesPerWord()) {
				rwc.codes[j] = s;
				k = 0;
				j++;
				s = 0;
			}
		}

		// Rotate last word
		if ((k < Coder.BITS_PER_LONGWORD) && (k != 0)) {
			s <<= Coder.BITS_PER_LONGWORD - (k << 1);
			rwc.codes[j] = s;
		}

		return rwc;
	}

	/**
	 * Set sequence
	 */
	@Override
	public void set(String seqStr) {
		set(seqStr, false);
	}

	public void set(String seqStr, boolean ignoreErrors) {
		if (seqStr == null) {
			length = 0;
			codes = null;
		} else {
			length = seqStr.length();
			int ilen = coder.length2words(length);
			codes = new long[ilen];
			char seqChar[] = seqStr.toCharArray();

			// Create binary sequence
			int j = 0, i = 0, k = 0;
			long s = 0;
			for (; i < seqChar.length; i++) {
				s <<= 2;
				s |= coder.baseToBits(seqChar[i], ignoreErrors);
				k++;
				if (k >= coder.basesPerWord()) {
					codes[j] = s;
					k = 0;
					j++;
					s = 0;
				}
			}

			// Last word: Shift the last bits
			if ((k < Coder.BITS_PER_LONGWORD) && (k != 0)) {
				s <<= Coder.BITS_PER_LONGWORD - (k << 1);
				codes[j] = s;
			}
		}
	}

	/**
	 * Replace a base in the sequence
	 */
	@Override
	public void setBase(int index, char base) {
		int idx = index / coder.basesPerWord();
		int pos = coder.lastBaseinWord() - (index % coder.basesPerWord());
		long newCode = coder.replaceBase(codes[idx], pos, base);
		codes[idx] = newCode;
	}

	public void setCodes(long[] codes) {
		this.codes = codes;
	}

	@Override
	public String toString() {
		return getSequence();
	}

	/**
	 * Write data in binary format
	 */
	@Override
	public void write(DataOutputStream dataOutStream) throws IOException {
		dataOutStream.writeInt(length);
		for (int i = 0; i < codes.length; i++)
			dataOutStream.writeLong(codes[i]);
	}
}
