package org.snpeff.fileIterator;

import org.snpeff.binseq.DnaSequence;

public class DnaSeqFileIterator extends BinSeqFileIterator<DnaSequence> {

	public DnaSeqFileIterator(String fileName) {
		super(fileName);
		readerObject = new DnaSequence(null);
	}

}
