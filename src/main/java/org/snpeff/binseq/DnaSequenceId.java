package org.snpeff.binseq;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * Binary packed DNA sequence with an ID (long)
 * 
 * @author pcingola
 */
public class DnaSequenceId extends DnaSequence {

	private static final long serialVersionUID = 452657339854548494L;
	private static DnaSequenceId EMPTY = null;

	long id;

	/**
	 * Empty sequence singleton
	 * @return
	 */
	public static DnaSequenceId empty() {
		if( EMPTY == null ) EMPTY = new DnaSequenceId("");
		return EMPTY;
	}

	public DnaSequenceId(long id, int length, long codes[]) {
		super(length, codes);
		this.id = id;
	}

	public DnaSequenceId(long id, String seqStr) {
		super(seqStr);
		this.id = id;
	}

	public DnaSequenceId(String seqStr) {
		super(seqStr);
		id = IdGenerator.id();
	}

	/**
	 * Create sequences
	 * @return
	 */
	@Override
	protected DnaSequence factory() {
		return new DnaSequenceId(id, null);
	}

	/**
	 * Create a new sequence
	 * @return
	 */
	@Override
	DnaSequence factory(int length, long codes[]) {
		return new DnaSequenceId(id, length, codes);
	}

	public long getId() {
		return id;
	}

	/**
	 * Read data in binary format 
	 * @param dataOutStream
	 * @throws IOException
	 */
	@Override
	public BinarySequence read(DataInputStream dataInStream) throws IOException {
		DnaSequenceId binSeq = new DnaSequenceId(null);
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
		id = dataInStream.readLong();
		super.readDataStream(dataInStream);
	}

	@Override
	public String toString() {
		return id + "\t" + getSequence();
	}

	/**
	 * Write data in binary format 
	 * @param dataOutStream
	 * @throws IOException
	 */
	@Override
	public void write(DataOutputStream dataOutStream) throws IOException {
		dataOutStream.writeLong(id);
		super.write(dataOutStream);
	}

}
