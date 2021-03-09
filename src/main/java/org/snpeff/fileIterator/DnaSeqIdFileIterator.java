package org.snpeff.fileIterator;

import org.snpeff.binseq.DnaSequenceId;
import org.snpeff.util.Log;

public class DnaSeqIdFileIterator extends BinSeqFileIterator<DnaSequenceId> {

	public DnaSeqIdFileIterator(String fileName) {
		super(fileName);
		Log.debug("SEQID");
		readerObject = new DnaSequenceId(null);
	}

}
