package org.snpeff.fileIterator;

import org.snpeff.binseq.DnaSequenceId;
import org.snpeff.util.Gpr;

public class DnaSeqIdFileIterator extends BinSeqFileIterator<DnaSequenceId> {

	public DnaSeqIdFileIterator(String fileName) {
		super(fileName);
		Gpr.debug("SEQID");
		readerObject = new DnaSequenceId(null);
	}

}
