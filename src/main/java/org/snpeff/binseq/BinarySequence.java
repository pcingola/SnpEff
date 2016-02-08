package org.snpeff.binseq;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.snpeff.binseq.coder.Coder;

/**
 * Base class for a binary 'read'. I.e. a binary representation of a sequencer's read
 * A read is a short DNA fragment (short now means anywhere from 36 bases to 10K).
 *
 * @author pcingola
 *
 */
public abstract class BinarySequence implements Comparable<BinarySequence>, Serializable, Cloneable {

	private static final long serialVersionUID = 2349094116844619569L;

	/**
	 * Return the base at position 'index'
	 */
	public char getBase(int index) {
		return getCoder().toBase(getCode(index));
	}

	@Override
	public BinarySequence clone() {
		try {
			return (BinarySequence) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Return the code at position 'index'
	 */
	public abstract int getCode(int index);

	/**
	 * Get sequence encoder & decoder
	 */
	public abstract Coder getCoder();

	/**
	 * Get the sequence as a String
	 */
	public abstract String getSequence();

	@Override
	public abstract int hashCode();

	/**
	 * Get sequence length
	 */
	public abstract int length();

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
	 */
	public BinarySequence overlap(BinarySequence sequence, int start) {
		throw new RuntimeException("Unimplemented!");
	}

	/**
	 * Read a sequence from a data stream
	 */
	public abstract BinarySequence read(DataInputStream dataInStream) throws IOException;

	protected abstract void readDataStream(DataInputStream dataInStream) throws IOException;

	/**
	 * Reverse Watson-Cricks complement
	 */
	public abstract BinarySequence reverseWc();

	/**
	 * Set sequence from a string
	 * @param seqStr
	 */
	public abstract void set(String seqStr);

	/**
	 * Set the base at position 'index'
	 * Note: This method is protected because we prefer BinarySequence to be 'immutable'
	 */
	void setBase(int index, char base) {
		throw new RuntimeException("Unimplemented!"); // By default sequence type is unmutable
	}

	/**
	 * Set the quality at position 'index'
	 */
	public void setQuality(int index, int quality) {
		throw new RuntimeException("Unimplemented!"); // By default sequence type is unmutable
	}

	/**
	 * Write to a binary stream
	 */
	public abstract void write(DataOutputStream dataOutStream) throws IOException;
}
