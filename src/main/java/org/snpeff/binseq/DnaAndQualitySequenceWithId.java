package org.snpeff.binseq;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.snpeff.fastq.FastqVariant;

/**
 * DnaAndQualitySequence with an ID
 * 
 * @author pcingola
 */
public class DnaAndQualitySequenceWithId extends DnaAndQualitySequence {

	private static final long serialVersionUID = 4523294567844568494L;
	private static DnaAndQualitySequenceWithId EMPTY = null;

	long id;

	public static DnaAndQualitySequenceWithId empty() {
		if( EMPTY == null ) EMPTY = new DnaAndQualitySequenceWithId("");
		return EMPTY;
	}

	public DnaAndQualitySequenceWithId(String seqStr) {
		super(seqStr);
		id = IdGenerator.id();
	}

	public DnaAndQualitySequenceWithId(String seqStr, String qualityStr, FastqVariant type) {
		super(seqStr, qualityStr, type);
		id = IdGenerator.id();
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
	protected void readDataStream(DataInputStream dataInStream) throws IOException {
		id = dataInStream.readLong();
		super.read(dataInStream);
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
