package ca.mcgill.mcb.pcingola.fileIterator;

import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.SeqChange;

/**
 * Opens a sequence change file and iterates over all sequence changes
 * 
 * @author pcingola
 */
public abstract class SeqChangeFileIterator extends MarkerFileIterator<SeqChange> {

	public SeqChangeFileIterator(String fileName, Genome genome, int inOffset) {
		super(fileName, genome, inOffset);
	}

	public SeqChangeFileIterator(String fileName, int inOffset) {
		super(fileName, inOffset);
	}
}
