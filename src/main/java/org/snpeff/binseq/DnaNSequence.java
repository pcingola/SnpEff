package org.snpeff.binseq;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.snpeff.binseq.coder.Coder;
import org.snpeff.collections.OpenBitSet;

/**
 * Binary packed DNA sequence that allows also 'N' bases: {A, C, G, T, N}
 * 
 * @author pcingola
 */
public class DnaNSequence extends DnaSequence {
	private static final long serialVersionUID = 843945646419836582L;

	OpenBitSet hasN; // A bit set indicating if there is an 'N'

	private static DnaNSequence EMPTY = null;

	/**
	 * Empty sequence singleton
	 */
	public static DnaNSequence empty() {
		if (EMPTY == null) EMPTY = new DnaNSequence("");
		return EMPTY;
	}

	public DnaNSequence(int length, long codes[]) {
		super(length, codes);
		hasN = new OpenBitSet(length);
	}

	public DnaNSequence(String seqStr) {
		super(null);
		if (seqStr == null) hasN = new OpenBitSet();
		else set(seqStr);
	}

	/**
	 * Create sequences
	 */
	@Override
	protected DnaNSequence factory() {
		return new DnaNSequence(null);
	}

	/**
	 * Create a new sequence
	 */
	@Override
	DnaSequence factory(int length, long codes[]) {
		return new DnaNSequence(length, codes);
	}

	@Override
	public char getBase(int index) {
		if (hasN.fastGet(index)) return 'N';
		return super.getBase(index);
	}

	/**
	 * Get a few bases from this sequence
	 */
	@Override
	public String getBases(int index, int len) {
		char bases[] = new char[len];
		int j = index / coder.basesPerWord();
		int k = coder.lastBaseinWord() - (index % coder.basesPerWord());
		for (int i = 0, idx = index; i < len; i++, idx++) {
			// Is it an 'N'?
			if (hasN.fastGet(idx)) bases[i] = 'N'; // It's an 'N'
			else bases[i] = coder.toBase(codes[j], k); // Decode base

			k--;
			if (k < 0) {
				k = coder.lastBaseinWord();
				j++;
			}
		}
		return new String(bases);
	}

	/**
	 * Read data in binary format 
	 */
	@Override
	public BinarySequence read(DataInputStream dataInStream) throws IOException {
		DnaNSequence binSeq = new DnaNSequence(null);
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
		super.readDataStream(dataInStream);

		// Read 'hasN' data
		long bits[] = new long[OpenBitSet.bits2words(length)];
		for (int i = 0; i < bits.length; i++)
			bits[i] = dataInStream.readLong();
		hasN = new OpenBitSet(bits, bits.length); // Create OpenBitSet
	}

	@Override
	public BinarySequence reverseWc() {
		DnaNSequence rwc = factory();
		rwc.codes = new long[codes.length];
		rwc.length = length;
		rwc.hasN.ensureCapacity(length);

		// Reverse all words and perform WC 
		int j = 0, k = 0;
		long s = 0;
		for (int index = length - 1, i = 0; index >= 0; index--, i++) {
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

			if (hasN.get(index)) rwc.hasN.set(i); // Update 'N'
		}

		// Rotate last word
		if ((k < Coder.BITS_PER_LONGWORD) && (k != 0)) {
			s <<= Coder.BITS_PER_LONGWORD - (k << 1);
			rwc.codes[j] = s;
		}

		return rwc;
	}

	@Override
	public void set(String seqStr) {
		super.set(seqStr, true); // Set the sequence, ignore errors

		// Set to 'hasN' wherever there is a coding error, i.e. base in the sequence is not {A, C, G, T}
		hasN = new OpenBitSet(seqStr.length());
		char seqChar[] = seqStr.toCharArray();
		for (int i = 0; i < seqChar.length; i++)
			switch (seqChar[i]) {
			case 'a':
			case 'A':
			case 'c':
			case 'C':
			case 'g':
			case 'G':
			case 't':
			case 'T':
			case 'u':
			case 'U':
				break;
			default:
				hasN.fastSet(i);
			}
	}

	/**
	 * Replace a base in the sequence
	 */
	@Override
	public void setBase(int index, char base) {
		if ((base == 'N') || (base == 'n')) {
			hasN.fastSet(index);
		} else {
			hasN.fastClear(index);
			super.setBase(index, base);
		}
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
		super.write(dataOutStream);

		// Store 'hasN' data
		long bits[] = hasN.getBits();
		for (int i = 0; i < bits.length; i++)
			dataOutStream.writeLong(bits[i]);
	}
}
