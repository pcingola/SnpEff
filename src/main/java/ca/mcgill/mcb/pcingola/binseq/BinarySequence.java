package ca.mcgill.mcb.pcingola.binseq;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import ca.mcgill.mcb.pcingola.binseq.coder.Coder;

/**
 * Base class for a binary 'read'. I.e. a binary representation of a sequencer's read
 * A read is a short DNA fragment (short now means anywhere from 36 bases to 10K).
 *  
 * @author pcingola
 *
 */
public abstract class BinarySequence implements Comparable<BinarySequence>, Serializable {

	private static final long serialVersionUID = 2349094116844619569L;

	/**
	 * Return the base at position 'index'
	 * @param index
	 * @return
	 */
	public char getBase(int index) {
		return getCoder().toBase(getCode(index));
	}

	/**
	 * Return the code at position 'index'
	 * @param index
	 * @return
	 */
	public abstract int getCode(int index);

	/**
	 * Get sequence encoder & decoder
	 * @return
	 */
	public abstract Coder getCoder();

	/**
	 * Get the sequence as a String
	 * @return
	 */
	public abstract String getSequence();

	@Override
	public abstract int hashCode();

	/**
	 * Get sequence length
	 * @return
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
	 * @param index
	 * @return
	 */
	public BinarySequence overlap(BinarySequence sequence, int start) {
		throw new RuntimeException("Unimplemented!");
	}

	/**
	 * Read a sequence from a data stream
	 * @param dataInStream
	 * @return
	 * @throws IOException
	 */
	public abstract BinarySequence read(DataInputStream dataInStream) throws IOException;

	protected abstract void readDataStream(DataInputStream dataInStream) throws IOException;

	/**
	 * Reverse Watson-Cricks complement 
	 * @return
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
	 * @param index
	 * @return
	 */
	void setBase(int index, char base) {
		throw new RuntimeException("Unimplemented!"); // By default sequence type is unmutable
	}

	/**
	 * Set the quality at position 'index'
	 * @param index
	 * @return
	 */
	public void setQuality(int index, int quality) {
		throw new RuntimeException("Unimplemented!"); // By default sequence type is unmutable
	}

	/**
	 * Write to a binary stream
	 * @param dataOutStream
	 * @throws IOException
	 */
	public abstract void write(DataOutputStream dataOutStream) throws IOException;
}
