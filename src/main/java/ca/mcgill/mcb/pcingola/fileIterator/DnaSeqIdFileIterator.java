package ca.mcgill.mcb.pcingola.fileIterator;

import ca.mcgill.mcb.pcingola.binseq.DnaSequenceId;
import ca.mcgill.mcb.pcingola.util.Gpr;

public class DnaSeqIdFileIterator extends BinSeqFileIterator<DnaSequenceId> {

	public DnaSeqIdFileIterator(String fileName) {
		super(fileName);
		Gpr.debug("SEQID");
		readerObject = new DnaSequenceId(null);
	}

}
