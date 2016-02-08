package org.snpeff.binseq;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.snpeff.binseq.coder.Coder;
import org.snpeff.binseq.coder.DnaCoder;

/**
 * Pair end DNA sequence (binary packed) 
 * It consists of 2 DNA sequences separated by a gap.
 * 
 * @author pcingola
 */
public class DnaSequencePe extends BinarySequence {

	private static final long serialVersionUID = -7267466011654235050L;
	public static final Coder coder = DnaCoder.get();

	DnaSequence seq1, seq2;
	int gap;

	public DnaSequencePe(String seqStr) {
		set(seqStr);
	}

	public DnaSequencePe(String seqStr1, String seqStr2, int gap) {
		seq1 = new DnaSequence(seqStr1);
		seq2 = new DnaSequence(seqStr2);
		this.gap = gap;
	}

	@Override
	public int compareTo(BinarySequence o) {
		DnaSequencePe bs = (DnaSequencePe) o;

		int c = seq1.compareTo(bs.seq1);
		if (c != 0) return c;

		if (gap < bs.gap) return 1;
		if (gap > bs.gap) return -1;

		return seq2.compareTo(bs.seq2);
	}

	@Override
	public int getCode(int index) {
		if (index < seq1.length()) return seq1.getCode(index);
		if (index < (seq1.length() + gap)) return 0;
		return seq2.getCode(index - seq1.length() - gap);
	}

	@Override
	public Coder getCoder() {
		return coder;
	}

	public int getGap() {
		return gap;
	}

	public DnaSequence getSeq1() {
		return seq1;
	}

	public DnaSequence getSeq2() {
		return seq2;
	}

	@Override
	public String getSequence() {
		StringBuilder sb = new StringBuilder();

		sb.append(seq1);
		for (int i = 0; i < gap; i++)
			sb.append('N');
		sb.append(seq2);

		return sb.toString();
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += seq1.hashCode();
		hash += 33 * hash + seq2.hashCode();
		hash += 33 * hash + gap;
		return hash;
	}

	/**
	 * Calculate the length of a sequence
	 * @param len
	 * @return
	 */
	public int intLen(int len) {
		return ((len % coder.basesPerWord()) != 0 ? len / coder.basesPerWord() + 1 : len / coder.basesPerWord());
	}

	@Override
	public int length() {
		return seq1.length() + seq2.length() + gap;
	}

	/**
	 * Read data in binary format 
	 * @param dataOutStream
	 * @throws IOException
	 */
	@Override
	public DnaSequencePe read(DataInputStream dataInStream) throws IOException {
		DnaSequencePe binSeq = new DnaSequencePe(null, null, 0);
		try {
			binSeq.readDataStream(dataInStream);
		} catch (EOFException e) {
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
		gap = dataInStream.readInt();

		seq1 = new DnaSequence(null);
		seq1.readDataStream(dataInStream);

		seq2 = new DnaSequence(null);
		seq2.readDataStream(dataInStream);
	}

	@Override
	public BinarySequence reverseWc() {
		DnaSequencePe seq = new DnaSequencePe(null, null, gap);
		DnaSequence s1rwc = (DnaSequence) seq1.reverseWc();
		DnaSequence s2rwc = (DnaSequence) seq2.reverseWc();
		seq.seq1 = s2rwc;
		seq.seq2 = s1rwc;
		return seq;
	}

	@Override
	public void set(String seqStr) {
		if (seqStr == null) {
			gap = 0;
			seq1 = seq2 = DnaSequence.empty();
			return;
		}

		String su = seqStr.toUpperCase();
		int idx1 = su.indexOf('N');
		int idx2 = su.lastIndexOf('N');
		gap = idx2 - idx1;

		if ((idx1 < 0) || (gap == 0)) { // No gap? => Just one sequence
			seq1 = new DnaSequence(seqStr);
			gap = 0;
			seq2 = DnaSequence.empty();
		} else {
			seq1 = new DnaSequence(su.substring(0, idx1));
			seq2 = new DnaSequence(su.substring(idx2 + 1));
		}
	}

	public void setGap(int gap) {
		this.gap = gap;
	}

	@Override
	public String toString() {
		return getSequence();
	}

	/**
	 * Write data in binary format 
	 * @param dataOutStream
	 * @throws IOException
	 */
	@Override
	public void write(DataOutputStream dataOutStream) throws IOException {
		dataOutStream.writeInt(gap);
		seq1.write(dataOutStream);
		seq2.write(dataOutStream);
	}
}
