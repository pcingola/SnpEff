package org.snpeff.binseq;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.snpeff.binseq.coder.Coder;
import org.snpeff.binseq.coder.DnaCoder;
import org.snpeff.binseq.coder.DnaQualityCoder;
import org.snpeff.fastq.Fastq;
import org.snpeff.fastq.FastqVariant;

/**
 * Binary packed DNA sequence and base calling quality
 * 
 * Notes:
 * 		- This is designed for short sequences (such as "short reads")
 * 		- Every base is encoded in 8 bits:
 * 			- Six bits for the base quality [0 , .. , 63]
 * 			- Two bits for the base {a, c, g, t} <=> {0, 1, 2, 3}
 * 		- All bits are stored in an array of 'bytes'
 * 
 * @author pcingola
 *
 */
public class DnaAndQualitySequence extends BinarySequence {

	private static final long serialVersionUID = 4523047567848438494L;
	private static DnaAndQualitySequence EMPTY = null;
	private static final DnaQualityCoder coder = DnaQualityCoder.get();

	byte codes[];

	public static DnaAndQualitySequence empty() {
		if( EMPTY == null ) EMPTY = new DnaAndQualitySequence("");
		return EMPTY;
	}

	public DnaAndQualitySequence(Fastq fastq) {
		set(fastq.getSequence(), fastq.getQuality(), fastq.getVariant());
	}

	public DnaAndQualitySequence(String seqStr) {
		if( seqStr != null ) set(seqStr);
	}

	public DnaAndQualitySequence(String seqStr, String qualityStr, FastqVariant type) {
		set(seqStr, qualityStr, type);
	}

	@Override
	public int compareTo(BinarySequence o) {
		DnaAndQualitySequence bs = (DnaAndQualitySequence) o;
		int minlen = Math.min(length(), bs.length());
		for( int i = 0; i < minlen; i++ ) {
			int b1 = (codes[i]) & 0x03;
			int b2 = (bs.codes[i]) & 0x03;

			if( b1 < b2 ) return -1;
			if( b1 > b2 ) return 1;
		}
		return 0;
	}

	@Override
	public char getBase(int index) {
		return DnaCoder.TO_BASE[codes[index] & 0x03];
	}

	@Override
	public int getCode(int index) {
		// if( (index < 0) || (index > codes.length) ) throw new IndexOutOfBoundsException("Index requested " + index + ", sequence length is " + codes.length);
		return codes[index] & (int) coder.mask(0);
	}

	@Override
	public Coder getCoder() {
		return coder;
	}

	public byte[] getCodes() {
		return codes;
	}

	/**
	 * Get quality string (encoded FastQ-Sanger style)
	 * @return
	 */
	public String getQuality() {
		StringBuilder sbq = new StringBuilder();
		for( int i = 0; i < codes.length; i++ ) {
			int q = coder.toQuality(codes[i]);
			char qchar = (char) ('!' + q);
			sbq.append(qchar);
		}
		return sbq.toString();
	}

	public int getQuality(int index) {
		return coder.toQuality(codes[index]);
	}

	@Override
	public String getSequence() {
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < codes.length; i++ ) {
			char c = coder.toBase(codes[i]);
			sb.append(c);
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		long hash = 0;
		for( int i = 0; i < codes.length; i++ )
			hash = hash * 33 + codes[i];

		return (int) hash;
	}

	/**
	 * Find the position of 'seq' in this sequence
	 * @param seq : String to be found
	 * @return The position where 'seq' is found or '-1' if not found
	 */
	public int indexOf(String seq) {
		return getSequence().indexOf(seq);
	}

	/**
	 * Returns the index within this string of the rightmost occurrence of the specified substring
	 */
	public int lastIndexOf(String seq) {
		return getSequence().lastIndexOf(seq);
	}

	/**
	 * Sequence lenth
	 * @return
	 */
	@Override
	public int length() {
		return codes.length;
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
	 * this.sequence  :  |xxxxxxxxxxxxxxxxxxxxOOOOOOOOOOOOOOOOOOOOOOOO|
	 * other.sequence :  |                    OOOOOOOOOOOOOOO         |
	 *                   |                    |start=20               |
	 * result         :  |xxxxxxxxxxxxxxxxxxxxOOOOOOOOOOOOOOOOOOOOOOOO|
	 *                   
	 * this.sequence  :  |                        OOOOOOOOOOO             |
	 * other.sequence :  |yyyyyyyyyyyyyyyyyyyyyyyyOOOOOOOOOOOOOOOOOOOOOOOO|
	 *                   |start=-20                                       |
	 * result         :  |yyyyyyyyyyyyyyyyyyyyyyyyOOOOOOOOOOOOOOOOOOOOOOOO|
	 * 
	 * @param start
	 * @return A new sequence
	 */
	@Override
	public DnaAndQualitySequence overlap(BinarySequence sequence, int start) {
		DnaAndQualitySequence seq = (DnaAndQualitySequence) sequence;

		// Create two references so that always seq1 starts before seq2 and start is positive
		DnaAndQualitySequence seq1, seq2;
		if( start >= 0 ) {
			seq1 = this;
			seq2 = seq;
		} else {
			seq1 = seq;
			seq2 = this;
			start = -start;
		}

		// Create new codes
		int len = Math.max(seq1.length(), start + seq2.length());
		byte[] newCodes = new byte[len];

		// Create a new sequence
		DnaAndQualitySequence newSeq = new DnaAndQualitySequence((String) null);
		newSeq.codes = newCodes;

		// Start overlapping
		for( int i = 0, j = -start; i < len; i++, j++ ) {
			if( j < 0 ) newCodes[i] = seq1.codes[i]; // We are not overlapping seq2 yet
			else {
				if( (i >= seq1.length()) && (j < seq2.length()) ) {
					// We are are done with seq1, use seq2
					newCodes[i] = seq2.codes[j];
				} else if( (i < seq1.length()) && (j >= seq2.length()) ) {
					// We are are done with seq2, use seq1
					newCodes[i] = seq1.codes[i];
				} else {
					// Overlap both sequences
					byte code1 = seq1.codes[i], code2 = seq2.codes[j];

					// Are both bases equal?
					if( coder.toBase(code1) == coder.toBase(code2) ) {
						// Add qualities
						int q = coder.toQuality(code1) + coder.toQuality(code2); // Add both quality scores
						newCodes[i] = (byte) coder.replaceQuality(code1, q); // Use new quality (both bases are equal)
					} else {
						// Not equal? Use highest quality
						int q = 0, q1 = coder.toQuality(code1), q2 = coder.toQuality(code2);
						char base = ' ';

						if( q1 >= q2 ) {
							q = q1;
							base = coder.toBase(code1);
						} else {
							q = q2;
							base = coder.toBase(code2);
						}

						newCodes[i] = (byte) coder.baseToBits(base, q);
					}
				}
			}
		}

		return newSeq;
	}

	/**
	 * Read data in binary format 
	 * @param dataOutStream
	 * @throws IOException
	 */
	@Override
	public DnaAndQualitySequence read(DataInputStream dataInStream) throws IOException {
		DnaAndQualitySequence binSeq = new DnaAndQualitySequence((String) null);
		try {
			binSeq.readDataStream(dataInStream);
		} catch(EOFException e) {
			return null;
		}
		return binSeq;
	}

	/**
	 * Read data in binary format 
	 * @param dataOutStream
	 * @throws IOException
	 */
	@Override
	protected void readDataStream(DataInputStream dataInStream) throws IOException {
		int length = dataInStream.readInt();
		codes = new byte[length];
		for( int i = 0; i < codes.length; i++ )
			codes[i] = dataInStream.readByte();
	}

	@Override
	public DnaAndQualitySequence reverseWc() {
		DnaAndQualitySequence rwc = new DnaAndQualitySequence((String) null);
		rwc.codes = new byte[codes.length];

		byte xorMask = (byte) 0x03;
		for( int i = 0; i < codes.length; i++ )
			rwc.codes[codes.length - i - 1] = (byte) (codes[i] ^ xorMask);

		return rwc;
	}

	/**
	 * Set sequence
	 * @param seqStr
	 */
	@Override
	public void set(String seqStr) {
		int length = seqStr.length();
		codes = new byte[length];
		char seqChar[] = seqStr.toCharArray();
		for( int i = 0; i < length; i++ )
			codes[i] = (byte) (coder.baseToBits(seqChar[i]) & 0xff);
	}

	/**
	 * Set sequence
	 * @param seqStr
	 */
	public void set(String seqStr, String qualityStr, FastqVariant type) {
		int length = seqStr.length();
		codes = new byte[length];
		char seqChar[] = seqStr.toCharArray();
		char qualChar[] = qualityStr.toCharArray();

		int qbase = 0;
		switch(type) {
			case FASTQ_SANGER:
				qbase = 33;
				break;
			case FASTQ_SOLEXA:
				qbase = 64;
				break;
			case FASTQ_ILLUMINA:
				qbase = 64;
				break;
			default:
				throw new RuntimeException("Unsuported quality type: " + type);
		}

		for( int i = 0; i < length; i++ ) {
			int q = Math.min(qualChar[i] - qbase, 63);
			if( (q < 0) && (type != FastqVariant.FASTQ_SOLEXA) ) throw new RuntimeException("Quality is below zero! Quality character: '" + qualChar[i] + "', quality=" + q + ", type '" + type + "'.");
			codes[i] = (byte) (coder.baseToBits(seqChar[i]) | coder.qualityToBits(q));
		}
	}

	@Override
	public String toString() {
		return getSequence() + "\t" + getQuality();
	}

	/**
	 * Write data in binary format 
	 * @param dataOutStream
	 * @throws IOException
	 */
	@Override
	public void write(DataOutputStream dataOutStream) throws IOException {
		dataOutStream.writeInt(codes.length);
		for( int i = 0; i < codes.length; i++ )
			dataOutStream.write(codes);
	}
}
