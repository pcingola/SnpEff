package ca.mcgill.mcb.pcingola.fileIterator;

import ca.mcgill.mcb.pcingola.binseq.DnaSequencePe;


public class DnaSeqPeFileIterator extends BinSeqFileIterator<DnaSequencePe> {

	public DnaSeqPeFileIterator(String fileName) {
		super(fileName);
		readerObject = new DnaSequencePe(null, null, 0);
	}
}
