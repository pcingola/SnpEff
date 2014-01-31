package ca.mcgill.mcb.pcingola.fileIterator;

import ca.mcgill.mcb.pcingola.binseq.DnaSequence;

public class DnaSeqFileIterator extends BinSeqFileIterator<DnaSequence> {

	public DnaSeqFileIterator(String fileName) {
		super(fileName);
		readerObject = new DnaSequence(null);
	}

}
