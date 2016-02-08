package org.snpeff.fileIterator;

import org.snpeff.binseq.DnaSequencePe;


public class DnaSeqPeFileIterator extends BinSeqFileIterator<DnaSequencePe> {

	public DnaSeqPeFileIterator(String fileName) {
		super(fileName);
		readerObject = new DnaSequencePe(null, null, 0);
	}
}
